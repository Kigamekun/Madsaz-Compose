package id.madsaz.app.feature.plan

import android.app.Activity
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import id.madsaz.app.R
import id.madsaz.app.data.billing.BillingEvent
import id.madsaz.app.data.billing.PlayBilling
import id.madsaz.app.data.remote.PlanApi
import id.madsaz.app.data.remote.PlanCatalog
import id.madsaz.app.data.remote.PlanStatus
import id.madsaz.app.ui.userMessageRes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

enum class PlanInterval { MONTHLY, YEARLY }

/** Harga dari Google Play: [formatted] untuk ditampilkan, [micros] untuk membandingkan paket. */
data class PlanPrice(val formatted: String, val micros: Long)

data class PlanUiState(
    /** null selama paket belum berhasil dimuat; layar tidak boleh menebak Free/Plus dari null. */
    val status: PlanStatus? = null,
    val prices: Map<PlanInterval, PlanPrice> = emptyMap(),
    val loading: Boolean = true,
    val purchasing: Boolean = false,
    val purchaseEnabled: Boolean = false,
    /** Id produk dari BE, dipakai untuk tautan kelola langganan di Google Play. */
    val productId: String? = null,
    @StringRes val errorRes: Int? = null,
    /** Hanya untuk indikator tarik-untuk-memuat-ulang; muat ulang otomatis tidak menampilkannya. */
    val refreshing: Boolean = false,
    /** Akun Google di Play Store perangkat ini sudah membayar Madsaz Plus untuk akun Madsaz lain (aturan Google: satu langganan per akun Google). */
    val playAccountInUse: Boolean = false,
    /** Penjelasan [playAccountInUse] sedang ditampilkan, menggantikan dialog Google yang hanya berkata "sudah berlangganan". */
    val showAccountInUse: Boolean = false,
) {
    /** Persen hemat paket tahunan terhadap 12 bulan paket bulanan, dihitung dari harga Play; null bila tidak lebih murah. */
    val yearlySavingsPercent: Int?
        get() {
            val monthly = prices[PlanInterval.MONTHLY]?.micros ?: return null
            val yearly = prices[PlanInterval.YEARLY]?.micros ?: return null
            return ((1.0 - yearly.toDouble() / (monthly * 12)) * 100).toInt().takeIf { it > 0 }
        }
}

class PlanViewModel(
    private val api: PlanApi,
    private val billing: PlayBilling,
    private val userId: String,
) : ViewModel() {
    var uiState by mutableStateOf(PlanUiState())
        private set

    private var catalog: PlanCatalog? = null
    private var product: ProductDetails? = null

    init {
        // Dimuat oleh layar setiap tampil (lihat MadsazApp), termasuk saat kembali dari Play Store setelah mengelola/membatalkan.
        viewModelScope.launch { billing.events.collect(::onBillingEvent) }
    }

    private var refreshJob: Job? = null

    fun refresh(pulled: Boolean = false) {
        // Muat ulang saat kembali dari Play dan saat Verified bisa tumpang tindih: hanya yang terbaru boleh menentukan status.
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            uiState = uiState.copy(loading = true, refreshing = pulled, errorRes = null)
            try {
                val (loadedCatalog, status) = coroutineScope {
                    val catalogRequest = async { billing.loadCatalog() }
                    // /plan memakai status langganan yang dicek ke Google paling lama sejam lalu; baca ulang dulu agar pembatalan langsung terlihat.
                    val statusRequest = async(Dispatchers.IO) { api.refreshSubscription(); api.status() }
                    catalogRequest.await() to statusRequest.await()
                }
                catalog = loadedCatalog
                uiState = uiState.copy(status = status, purchaseEnabled = loadedCatalog.purchaseEnabled, productId = loadedCatalog.productId, loading = false, refreshing = false)
                loadPrices(loadedCatalog)
                // Pembelian yang sudah dimiliki akun Google tetapi belum diketahui BE (mis. aplikasi ditutup sebelum verifikasi).
                uiState = uiState.copy(playAccountInUse = billing.sync(userId))
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                uiState = uiState.copy(loading = false, refreshing = false, errorRes = error.userMessageRes())
            }
        }
    }

    /** [despiteAccountInUse]: pengguna sudah membaca penjelasan dan tetap mencoba (jendela Google di sebagian perangkat bisa ganti akun). */
    fun purchase(activity: Activity, interval: PlanInterval, despiteAccountInUse: Boolean = false) {
        if (uiState.playAccountInUse && !despiteAccountInUse) {
            uiState = uiState.copy(showAccountInUse = true, errorRes = null)
            return
        }
        uiState = uiState.copy(showAccountInUse = false)
        val loadedCatalog = catalog
        val details = product
        val basePlanId = when (interval) {
            PlanInterval.MONTHLY -> loadedCatalog?.monthlyBasePlanId
            PlanInterval.YEARLY -> loadedCatalog?.yearlyBasePlanId
        }
        val offer = details?.subscriptionOfferDetails?.firstOrNull { it.basePlanId == basePlanId && it.offerId == null }
        if (!uiState.purchaseEnabled || details == null || offer == null) {
            uiState = uiState.copy(errorRes = R.string.error_plan_unavailable)
            return
        }
        uiState = uiState.copy(purchasing = true, errorRes = null)
        // Peluncuran yang ditolak, dibatalkan, atau sudah dimiliki dilaporkan lewat billing.events.
        val result = billing.launchPurchase(activity, details, offer.offerToken, userId)
        if (result.responseCode != BillingClient.BillingResponseCode.OK && result.responseCode != BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            uiState = uiState.copy(purchasing = false, errorRes = R.string.error_billing_failed)
        } else if (result.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            restore()
        }
    }

    private suspend fun loadPrices(loadedCatalog: PlanCatalog) {
        val details = billing.subscriptionProduct(loadedCatalog.productId)
        product = details
        val prices = buildMap {
            details?.subscriptionOfferDetails.orEmpty().filter { it.offerId == null }.forEach { offer ->
                val interval = when (offer.basePlanId) {
                    loadedCatalog.monthlyBasePlanId -> PlanInterval.MONTHLY
                    loadedCatalog.yearlyBasePlanId -> PlanInterval.YEARLY
                    else -> return@forEach
                }
                // Penawaran tanpa fase harga tidak bisa ditampilkan; dibiarkan tanpa harga sehingga tombol beli nonaktif.
                offer.pricingPhases.pricingPhaseList.lastOrNull()?.let { put(interval, PlanPrice(it.formattedPrice, it.priceAmountMicros)) }
            }
        }
        uiState = uiState.copy(prices = prices)
    }

    fun dismissAccountInUse() {
        uiState = uiState.copy(showAccountInUse = false)
    }

    private fun restore() {
        viewModelScope.launch {
            try {
                onAlreadyOwned(byOtherAccount = billing.sync(userId, force = true))
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                uiState = uiState.copy(purchasing = false, errorRes = error.userMessageRes())
            }
        }
    }

    private fun onBillingEvent(event: BillingEvent) {
        when (event) {
            BillingEvent.Verified -> {
                uiState = uiState.copy(purchasing = false)
                refresh()
            }
            BillingEvent.Pending -> uiState = uiState.copy(purchasing = false, errorRes = R.string.error_billing_pending)
            BillingEvent.Canceled -> uiState = uiState.copy(purchasing = false)
            is BillingEvent.AlreadyOwned -> onAlreadyOwned(event.byOtherAccount)
            is BillingEvent.Failed -> uiState = uiState.copy(purchasing = false, errorRes = event.cause?.userMessageRes() ?: R.string.error_billing_failed)
        }
    }

    // Milik akun ini sudah diverifikasi ulang di dalam sync (Verified memuat ulang paket); milik akun lain dijelaskan.
    private fun onAlreadyOwned(byOtherAccount: Boolean) {
        uiState = uiState.copy(purchasing = false, playAccountInUse = byOtherAccount, showAccountInUse = byOtherAccount)
    }
}
