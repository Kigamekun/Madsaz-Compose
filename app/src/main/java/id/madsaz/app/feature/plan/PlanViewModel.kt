package id.madsaz.app.feature.plan

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.Purchase
import id.madsaz.app.data.remote.PlanApi
import id.madsaz.app.data.remote.PlanStatus
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PlanUiState(
    val status: PlanStatus? = null,
    val prices: Map<String, String> = emptyMap(),
    val loading: Boolean = true,
    val purchasing: Boolean = false,
    val purchaseEnabled: Boolean = false,
    val error: String? = null,
)

class PlanViewModel(
    context: Context,
    private val accessToken: String,
    private val userId: String,
    private val api: PlanApi = PlanApi(),
) : ViewModel() {
    var uiState by mutableStateOf(PlanUiState())
        private set

    private var productId = "madsaz_plus"
    private var monthlyBasePlanId = "monthly"
    private var yearlyBasePlanId = "yearly"
    private var catalogLoaded = false
    private var product: ProductDetails? = null
    private val billingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.orEmpty().filter { productId in it.products }.forEach(::verifyPurchase)
            } else if (result.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
                uiState = uiState.copy(purchasing = false, error = result.debugMessage.ifBlank { "Pembayaran gagal." })
            } else {
                uiState = uiState.copy(purchasing = false)
            }
        }
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    init {
        refresh()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    if (catalogLoaded) {
                        loadProducts()
                        restorePurchases()
                    }
                } else {
                    uiState = uiState.copy(error = "Google Play Billing belum tersedia di perangkat ini.")
                }
            }

            override fun onBillingServiceDisconnected() {
                uiState = uiState.copy(error = "Koneksi Google Play terputus. Buka kembali halaman paket.")
            }
        })
    }

    fun refresh() {
        viewModelScope.launch {
            uiState = uiState.copy(loading = true, error = null)
            try {
                val (catalog, status) = withContext(Dispatchers.IO) { api.catalog(accessToken) to api.status(accessToken) }
                productId = catalog.productId
                monthlyBasePlanId = catalog.monthlyBasePlanId
                yearlyBasePlanId = catalog.yearlyBasePlanId
                catalogLoaded = true
                uiState = uiState.copy(status = status, purchaseEnabled = catalog.purchaseEnabled, loading = false)
                if (billingClient.isReady) {
                    loadProducts()
                    restorePurchases()
                }
            } catch (error: Exception) {
                uiState = uiState.copy(loading = false, error = error.message ?: "Paket gagal dimuat.")
            }
        }
    }

    fun purchase(activity: Activity, basePlanId: String) {
        if (!uiState.purchaseEnabled) return
        val details = product ?: return
        val configuredBasePlanId = if (basePlanId == "monthly") monthlyBasePlanId else yearlyBasePlanId
        val offer = details.subscriptionOfferDetails?.firstOrNull {
            it.basePlanId == configuredBasePlanId && it.offerId == null
        } ?: return
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(details)
                    .setOfferToken(offer.offerToken)
                    .build(),
            ))
            .setObfuscatedAccountId(MessageDigest.getInstance("SHA-256")
                .digest(userId.toByteArray()).joinToString("") { "%02x".format(it.toInt() and 0xff) })
            .build()
        uiState = uiState.copy(purchasing = true, error = null)
        val result = billingClient.launchBillingFlow(activity, params)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            uiState = uiState.copy(purchasing = false, error = result.debugMessage.ifBlank { "Pembayaran tidak dapat dibuka." })
        }
    }

    private fun loadProducts() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId).setProductType(BillingClient.ProductType.SUBS).build()))
            .build()
        billingClient.queryProductDetailsAsync(params) { result, response ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                uiState = uiState.copy(error = "Harga paket belum dapat dimuat dari Google Play.")
                return@queryProductDetailsAsync
            }
            product = response.productDetailsList.firstOrNull()
            val prices = product?.subscriptionOfferDetails.orEmpty()
                .filter { it.offerId == null }
                .associate { offer ->
                    (if (offer.basePlanId == monthlyBasePlanId) "monthly" else if (offer.basePlanId == yearlyBasePlanId) "yearly" else offer.basePlanId) to
                        (offer.pricingPhases.pricingPhaseList.lastOrNull()?.formattedPrice ?: "")
                }
            uiState = uiState.copy(prices = prices)
        }
    }

    private fun restorePurchases() {
        billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS).build()) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.filter { productId in it.products }.forEach(::verifyPurchase)
            }
        }
    }

    private fun verifyPurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) {
            uiState = uiState.copy(purchasing = false, error = "Pembayaran masih menunggu konfirmasi Google Play.")
            return
        }
        viewModelScope.launch {
            try {
                val status = withContext(Dispatchers.IO) { api.verify(accessToken, purchase.purchaseToken) }
                uiState = uiState.copy(status = status, purchasing = false, error = null)
            } catch (error: Exception) {
                uiState = uiState.copy(purchasing = false, error = error.message ?: "Pembelian belum dapat diverifikasi.")
            }
        }
    }

    override fun onCleared() {
        billingClient.endConnection()
    }
}
