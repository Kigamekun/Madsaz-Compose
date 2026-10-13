package id.madsaz.app.data.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsResult
import com.android.billingclient.api.QueryPurchasesParams
import id.madsaz.app.data.remote.ApiException
import id.madsaz.app.data.remote.PlanApi
import id.madsaz.app.data.remote.PlanCatalog
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private const val TAG = "PlayBilling"

/** Play Billing tidak bisa dipakai (layanan tidak tersedia di perangkat, atau kueri ditolak). */
class BillingException(val responseCode: Int) : Exception("Play Billing response code $responseCode")

/** Hasil pembelian yang sudah ditangani di sini; layar Paket menerjemahkannya ke tampilan. */
sealed interface BillingEvent {
    /** BE sudah memverifikasi token ke Google Play dan mengaktifkan Plus. */
    data object Verified : BillingEvent
    data object Pending : BillingEvent
    data object Canceled : BillingEvent

    /** [cause] null berarti Play Billing menolak; selain itu kegagalan verifikasi ke BE. */
    data class Failed(val cause: Throwable?) : BillingEvent
}

/**
 * Satu-satunya BillingClient aplikasi. Panduan Play: pertahankan satu koneksi aktif sejak aplikasi dibuka supaya listener
 * menerima pembelian di luar layar Paket (pembayaran tertunda yang selesai, kode promo), dan baca ulang pembelian milik
 * pengguna saat terhubung dan tiap aplikasi kembali aktif. Hak akses Plus tetap diputuskan BE setelah token diverifikasi ke Google.
 */
class PlayBilling(context: Context, private val api: PlanApi) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val connectLock = Mutex()
    private val verifiedTokens = ConcurrentHashMap.newKeySet<String>()
    private val mutableEvents = MutableSharedFlow<BillingEvent>(extraBufferCapacity = 4)

    @Volatile private var userId: String? = null
    @Volatile private var catalog: PlanCatalog? = null

    val events: SharedFlow<BillingEvent> = mutableEvents

    private val client = BillingClient.newBuilder(context.applicationContext)
        .setListener { result, purchases -> onPurchasesUpdated(result, purchases.orEmpty()) }
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection()
        .build()

    /** Katalog dari BE selalu diambil ulang (purchase_enabled bisa berubah) dan disimpan untuk pembelian di luar layar Paket. */
    suspend fun loadCatalog(): PlanCatalog = withContext(Dispatchers.IO) { api.catalog() }.also { catalog = it }

    suspend fun subscriptionProduct(productId: String): ProductDetails? {
        ensureConnected()
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId).setProductType(BillingClient.ProductType.SUBS).build()))
            .build()
        val (result, response) = suspendCancellableCoroutine<Pair<BillingResult, QueryProductDetailsResult>> { continuation ->
            client.queryProductDetailsAsync(params) { billingResult, details -> continuation.resume(billingResult to details) }
        }
        if (result.responseCode != BillingClient.BillingResponseCode.OK) throw BillingException(result.responseCode)
        return response.productDetailsList.firstOrNull()
    }

    /** Hasil peluncuran dilaporkan lewat [events] (OK berarti dialog Google Play tampil). */
    fun launchPurchase(activity: Activity, details: ProductDetails, offerToken: String, userId: String): BillingResult {
        this.userId = userId
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details).setOfferToken(offerToken).build(),
            ))
            // BE mencocokkan nilai ini dengan identitas pembelian sebelum memberi akses.
            .setObfuscatedAccountId(MessageDigest.getInstance("SHA-256")
                .digest(userId.toByteArray()).joinToString("") { "%02x".format(it.toInt() and 0xff) })
            .build()
        return client.launchBillingFlow(activity, params)
    }

    /**
     * Memverifikasi ke BE pembelian Madsaz Plus yang dimiliki akun Google di perangkat ini. Tiap token hanya sekali per sesi;
     * [force] mengulang (pengguna memintanya, atau Play melaporkan produk sudah dimiliki).
     */
    suspend fun sync(userId: String, force: Boolean = false) {
        this.userId = userId
        if (force) verifiedTokens.clear()
        val productId = (catalog ?: loadCatalog()).productId
        ensureConnected()
        val (result, purchases) = suspendCancellableCoroutine<Pair<BillingResult, List<Purchase>>> { continuation ->
            client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()) { billingResult, owned ->
                continuation.resume(billingResult to owned)
            }
        }
        if (result.responseCode != BillingClient.BillingResponseCode.OK) throw BillingException(result.responseCode)
        purchases.filter { productId in it.products }.forEach { handle(it, userId) }
    }

    private suspend fun ensureConnected() = connectLock.withLock {
        if (client.isReady) return@withLock
        val code = suspendCancellableCoroutine<Int> { continuation ->
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (continuation.isActive) continuation.resume(result.responseCode)
                }

                // Sambungan ulang otomatis (enableAutoServiceReconnection); tidak boleh memanggil startConnection lagi di sini.
                override fun onBillingServiceDisconnected() = Unit
            })
        }
        if (code != BillingClient.BillingResponseCode.OK) throw BillingException(code)
    }

    private fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> scope.launch {
                val user = userId ?: return@launch
                val productId = catalog?.productId ?: return@launch
                purchases.filter { productId in it.products }.forEach { handle(it, user) }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> mutableEvents.tryEmit(BillingEvent.Canceled)
            // Produk sudah dimiliki akun Google ini: verifikasi ulang miliknya ke BE alih-alih menampilkan galat.
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> scope.launch {
                val user = userId ?: return@launch
                try {
                    sync(user, force = true)
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    mutableEvents.emit(BillingEvent.Failed(error))
                }
            }
            else -> {
                Log.w(TAG, "Purchase update failed: ${result.responseCode} ${result.debugMessage}")
                mutableEvents.tryEmit(BillingEvent.Failed(null))
            }
        }
    }

    private suspend fun handle(purchase: Purchase, userId: String) {
        when (purchase.purchaseState) {
            // Akses hanya untuk PURCHASED; token PENDING baru diverifikasi setelah Google menyelesaikannya.
            Purchase.PurchaseState.PENDING -> mutableEvents.emit(BillingEvent.Pending)
            Purchase.PurchaseState.PURCHASED -> verify(purchase.purchaseToken, "$userId:${purchase.purchaseToken}")
        }
    }

    private suspend fun verify(purchaseToken: String, key: String) {
        if (!verifiedTokens.add(key)) return
        try {
            withContext(Dispatchers.IO) { api.verify(purchaseToken) }
            mutableEvents.emit(BillingEvent.Verified)
        } catch (error: CancellationException) {
            verifiedTokens.remove(key)
            throw error
        } catch (error: Exception) {
            // Ditolak BE (4xx, mis. milik akun lain) tidak akan berhasil bila diulang; jaringan atau server (5xx) boleh dicoba lagi.
            if (error !is ApiException || error.status >= 500) verifiedTokens.remove(key)
            Log.w(TAG, "Purchase verification failed", error)
            mutableEvents.emit(BillingEvent.Failed(error))
        }
    }
}
