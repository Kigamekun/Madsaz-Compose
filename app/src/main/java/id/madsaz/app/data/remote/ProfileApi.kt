package id.madsaz.app.data.remote

import id.madsaz.app.BuildConfig
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

data class BabyProfile(
    val id: String,
    val name: String,
    val birthDate: String?,
    val weightKg: Double?,
)

class ProfileApi {
    fun getBabyProfile(accessToken: String): BabyProfile =
        request("GET", "/profile/baby", accessToken).let(::parseBabyProfile)

    fun updateBabyProfile(
        accessToken: String,
        name: String,
        birthDate: String?,
        weightKg: Double?,
    ): BabyProfile {
        val body = JSONObject().put("name", name)
        if (birthDate.isNullOrBlank()) body.put("birthDate", JSONObject.NULL) else body.put("birthDate", birthDate)
        if (weightKg == null) body.put("weightKg", JSONObject.NULL) else body.put("weightKg", weightKg)
        return parseBabyProfile(request("PUT", "/profile/baby", accessToken, body))
    }

    private fun request(method: String, path: String, token: String, body: JSONObject? = null): JSONObject {
        val connection = (URL(BuildConfig.API_BASE_URL.trimEnd('/') + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 10_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $token")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
        }
        try {
            if (body != null) connection.outputStream.use { it.write(body.toString().toByteArray()) }
            val responseBody = (if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (connection.responseCode !in 200..299) {
                val message = runCatching { JSONObject(responseBody).optString("error") }.getOrNull()
                throw IllegalStateException(message?.ifBlank { null } ?: "Profil bayi gagal dimuat (${connection.responseCode})")
            }
            return JSONObject(responseBody).getJSONObject("data")
        } finally {
            connection.disconnect()
        }
    }

    private fun parseBabyProfile(json: JSONObject) = BabyProfile(
        id = json.getString("id"),
        name = json.getString("name"),
        birthDate = if (json.isNull("birthDate")) null else runCatching {
            LocalDate.parse(json.getString("birthDate").take(10)).toString()
        }.getOrNull(),
        weightKg = if (json.isNull("weightKg")) null else json.getDouble("weightKg"),
    )
}
