package id.madsaz.app.data.remote

import id.madsaz.app.BuildConfig
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class NotificationApi {
    fun register(accessToken: String, fcmToken: String) = request("POST", accessToken, fcmToken)
    fun unregister(accessToken: String, fcmToken: String) = request("DELETE", accessToken, fcmToken)

    private fun request(method: String, accessToken: String, fcmToken: String) {
        val connection = (URL(BuildConfig.API_BASE_URL.trimEnd('/') + "/notifications/device")
            .openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 10_000
            readTimeout = 15_000
            doOutput = true
            setRequestProperty("Authorization", "Bearer $accessToken")
            setRequestProperty("Content-Type", "application/json")
        }
        try {
            connection.outputStream.use { it.write(JSONObject().put("token", fcmToken).toString().toByteArray()) }
            if (connection.responseCode !in 200..299) {
                throw IllegalStateException("Pengaturan notifikasi gagal (${connection.responseCode})")
            }
        } finally {
            connection.disconnect()
        }
    }
}
