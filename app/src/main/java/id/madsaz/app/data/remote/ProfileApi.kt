package id.madsaz.app.data.remote

import id.madsaz.app.BuildConfig
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

data class AccountProfile(val id: String, val name: String, val email: String, val avatarUrl: String?)

class ProfileApi {
    fun get(accessToken: String): AccountProfile = parse(request("GET", "/profile", accessToken))

    fun updateName(accessToken: String, name: String): AccountProfile =
        parse(request("PUT", "/profile", accessToken, JSONObject().put("name", name)))

    fun uploadAvatar(accessToken: String, jpeg: ByteArray): AccountProfile {
        val data = request("POST", "/profile/avatar-upload-url", accessToken,
            JSONObject().put("size_bytes", jpeg.size))
        val upload = (URL(data.getString("uploadUrl")).openConnection() as HttpURLConnection).apply {
            requestMethod = "PUT"
            doOutput = true
            connectTimeout = 10_000
            readTimeout = 60_000
            setFixedLengthStreamingMode(jpeg.size)
            setRequestProperty("Content-Type", "image/jpeg")
        }
        try {
            upload.outputStream.use { it.write(jpeg) }
            if (upload.responseCode !in 200..299) throw IllegalStateException("Foto gagal diunggah (${upload.responseCode})")
        } finally {
            upload.disconnect()
        }
        return get(accessToken)
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
            val status = connection.responseCode
            val response = (if (status in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (status !in 200..299) {
                val message = runCatching { JSONObject(response).optString("error") }.getOrNull()
                throw IllegalStateException(message?.ifBlank { null } ?: "Profil gagal dimuat ($status)")
            }
            return JSONObject(response).getJSONObject("data")
        } finally {
            connection.disconnect()
        }
    }

    private fun parse(json: JSONObject) = AccountProfile(
        id = json.getString("id"),
        name = json.optString("name"),
        email = json.getString("email"),
        avatarUrl = json.optString("avatarUrl").ifBlank { null },
    )
}
