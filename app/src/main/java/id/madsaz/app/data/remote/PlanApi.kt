package id.madsaz.app.data.remote

import org.json.JSONObject
import java.time.Instant

/** Pemakaian paket bulan ini dari GET /plan: paket Free punya batas dan sisa, paket Plus punya masa aktif dan status perpanjangan. */
sealed interface PlanStatus {
    val used: Int

    data class Free(override val used: Int, val limit: Int, val remaining: Int) : PlanStatus
    data class Plus(override val used: Int, val expiresAt: Instant?, val autoRenewing: Boolean) : PlanStatus
}

/** Id produk Google Play dari BE; app tidak menebak id sendiri. */
data class PlanCatalog(
    val productId: String,
    val monthlyBasePlanId: String,
    val quarterlyBasePlanId: String,
    val yearlyBasePlanId: String,
    val purchaseEnabled: Boolean,
)

class PlanApi(private val client: ApiClient) {
    fun catalog(): PlanCatalog {
        val plus = client.requestJson("GET", "/payments/plans").obj("data").obj("plus")
        val catalog = PlanCatalog(
            productId = plus.string("product_id"),
            monthlyBasePlanId = plus.string("monthly_base_plan_id"),
            quarterlyBasePlanId = plus.string("quarterly_base_plan_id"),
            yearlyBasePlanId = plus.string("yearly_base_plan_id"),
            purchaseEnabled = plus.boolean("purchase_enabled"),
        )
        // Satu id untuk dua paket membuat harga dan pembelian menempel ke paket yang salah tanpa tanda apa pun.
        val basePlanIds = listOf(catalog.monthlyBasePlanId, catalog.quarterlyBasePlanId, catalog.yearlyBasePlanId)
        if (basePlanIds.toSet().size != basePlanIds.size) throw ContractException("Plus base plan ids must be distinct")
        return catalog
    }

    fun status(): PlanStatus {
        val plan = client.requestJson("GET", "/plan").obj("data")
        val used = plan.int("used")
        return when (val code = plan.string("code")) {
            "plus" -> PlanStatus.Plus(used, plan.stringOrNull("expires_at")?.let(Instant::parse), plan.boolean("auto_renewing"))
            "free" -> PlanStatus.Free(used, limit = plan.int("limit"), remaining = plan.int("remaining"))
            else -> throw ContractException("Unknown plan code '$code'")
        }
    }

    /**
     * Meminta BE membaca ulang langganan dari Google sekarang (GET /plan memakai cache satu jam), misalnya setelah pengguna
     * membatalkan di Play Store. Body diabaikan; ambil [status] sesudahnya.
     */
    fun refreshSubscription() {
        client.requestJson("GET", "/payments/subscription")
    }

    /** BE memverifikasi token pembelian ke Google Play dan mengaktifkan Plus. Ambil [status] lagi sesudahnya. */
    fun verify(purchaseToken: String) {
        client.requestJson("POST", "/payments/google-play/verify", JSONObject().put("purchaseToken", purchaseToken))
    }
}
