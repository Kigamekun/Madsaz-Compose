package id.madsaz.app.data.remote

import id.madsaz.app.BuildConfig
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class PlanStatus(
    val code: String,
    val used: Int,
    val limit: Int?,
    val remaining: Int?,
    val status: String,
    val interval: String?,
    val expiresAt: String?,
    val autoRenewing: Boolean,
)

data class PlanCatalog(val productId: String, val monthlyBasePlanId: String, val yearlyBasePlanId: String, val purchaseEnabled: Boolean)

class PlanApi {
    fun catalog(accessToken: String): PlanCatalog {
        val plus = request("GET", "/payments/plans", accessToken).getJSONObject("plus")
        return PlanCatalog(
            productId = plus.getString("product_id"),
            monthlyBasePlanId = plus.getString("monthly_base_plan_id"),
            yearlyBasePlanId = plus.getString("yearly_base_plan_id"),
            purchaseEnabled = plus.optBoolean("purchase_enabled"),
        )
    }

    fun status(accessToken: String): PlanStatus {
        val subscription = request("GET", "/payments/subscription", accessToken)
        val plan = request("GET", "/plan", accessToken)
        return PlanStatus(
            code = subscription.getString("plan_code"),
            used = plan.getInt("used"),
            limit = if (plan.isNull("limit")) null else plan.getInt("limit"),
            remaining = if (plan.isNull("remaining")) null else plan.getInt("remaining"),
            status = subscription.getString("status"),
            interval = subscription.optString("interval").ifBlank { null },
            expiresAt = subscription.optString("expires_at").ifBlank { null },
            autoRenewing = subscription.optBoolean("auto_renewing"),
        )
    }

    fun verify(accessToken: String, purchaseToken: String): PlanStatus {
        request("POST", "/payments/google-play/verify", accessToken, JSONObject().put("purchaseToken", purchaseToken))
        return status(accessToken)
    }

    private fun request(method: String, path: String, token: String, body: JSONObject? = null): JSONObject {
        val connection = (URL(BuildConfig.API_BASE_URL.trimEnd('/') + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 10_000
            readTimeout = 20_000
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Accept", "application/json")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
        }
        try {
            if (body != null) connection.outputStream.use { it.write(body.toString().toByteArray()) }
            val response = (if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (connection.responseCode !in 200..299) {
                val message = runCatching { JSONObject(response).optString("error") }.getOrNull()
                throw IllegalStateException(message?.ifBlank { null } ?: "Server error (${connection.responseCode})")
            }
            return JSONObject(response).getJSONObject("data")
        } finally {
            connection.disconnect()
        }
    }
}
