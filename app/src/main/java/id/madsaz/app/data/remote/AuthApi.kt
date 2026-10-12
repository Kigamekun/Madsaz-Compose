package id.madsaz.app.data.remote

import id.madsaz.app.BuildConfig
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class AuthResponse(
    val userId: String,
    val accessToken: String,
    val refreshToken: String,
    val email: String,
    val name: String?,
)

class AuthApiException(val statusCode: Int, message: String) : IllegalStateException(message)

class AuthApi {
    fun login(email: String, password: String): AuthResponse = requireNotNull(request(
        path = "/auth/login",
        body = JSONObject().put("email", email).put("password", password),
    ))

    fun register(name: String, email: String, password: String): AuthResponse = requireNotNull(request(
        path = "/auth/register",
        body = JSONObject().put("name", name).put("email", email).put("password", password),
    ))

    fun refresh(refreshToken: String): AuthResponse = requireNotNull(request(
        path = "/auth/refresh",
        body = JSONObject().put("refreshToken", refreshToken),
    ))

    fun logout(refreshToken: String) {
        request(path = "/auth/logout", body = JSONObject().put("refreshToken", refreshToken), expectBody = false)
    }

    private fun request(path: String, body: JSONObject, expectBody: Boolean = true): AuthResponse? {
        val connection = (URL(BuildConfig.API_BASE_URL.trimEnd('/') + path).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10_000
            readTimeout = 15_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }

        try {
            connection.outputStream.use { it.write(body.toString().toByteArray()) }
            val responseBody = (if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()
                ?.use { it.readText() }
                .orEmpty()

            if (connection.responseCode !in 200..299) {
                val message = runCatching { JSONObject(responseBody).optString("error") }.getOrNull()
                throw AuthApiException(
                    connection.responseCode,
                    message?.ifBlank { null } ?: "Permintaan auth gagal (${connection.responseCode})",
                )
            }

            if (!expectBody) return null
            val json = JSONObject(responseBody)
            val user = json.getJSONObject("user")
            return AuthResponse(
                userId = user.getString("id"),
                accessToken = json.getString("accessToken"),
                refreshToken = json.getString("refreshToken"),
                email = user.getString("email"),
                name = user.optString("name").ifBlank { null },
            )
        } finally {
            connection.disconnect()
        }
    }
}
