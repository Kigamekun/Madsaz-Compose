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
        viewModelScope.launch { billing.events.collect(::onBillingEvent) }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            uiState = uiState.copy(loading = true, errorRes = null)
            try {
                val (loadedCatalog, status) = coroutineScope {
                    val catalogRequest = async { billing.loadCatalog() }
                    val statusRequest = async(Dispatchers.IO) { api.status() }
                    catalogRequest.await() to statusRequest.await()
                }
                catalog = loadedCatalog
                uiState = uiState.copy(status = status, purchaseEnabled = loadedCatalog.purchaseEnabled, productId = loadedCatalog.productId, loading = false)
                loadPrices(loadedCatalog)
                // Pembelian yang sudah dimiliki akun Google tetapi belum diketahui BE (mis. aplikasi ditutup sebelum verifikasi).
                billing.sync(userId)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                uiState = uiState.copy(loading = false, errorRes = error.userMessageRes())
            }
        }
    }

    fun purchase(activity: Activity, interval: PlanInterval) {
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

    private fun restore() {
        viewModelScope.launch {
            try {
                billing.sync(userId, force = true)
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
            is BillingEvent.Failed -> uiState = uiState.copy(purchasing = false, errorRes = event.cause?.userMessageRes() ?: R.string.error_billing_failed)
        }
    }
}
