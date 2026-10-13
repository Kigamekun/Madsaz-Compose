package id.madsaz.app.data.remote

import org.json.JSONException
import org.json.JSONObject
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

/** Respons non-2xx dari BE: `{ "error": string, "code"?: string }`. Teks `error` berbahasa Inggris dan tidak ditampilkan ke pengguna. */
class ApiException(val status: Int, val code: String?, message: String) : Exception(message)

/** Pemilik access token. Dipisah dari [ApiClient] agar AuthApi (tanpa token) dan SessionManager tidak saling bergantung. */
interface Credentials {
    fun accessToken(): String

    /** Dipanggil setelah 401. Kembali bila token sudah diperbarui; melempar [ApiException] bila sesi berakhir. */
    fun refreshAfterUnauthorized(failedToken: String)
}

/**
 * Satu-satunya tempat HTTP ke API Madsaz. Blocking: panggil dari Dispatchers.IO.
 * Dengan [credentials], 401 memicu satu kali refresh token lalu satu kali ulang permintaan.
 */
class ApiClient(private val baseUrl: String, private val credentials: Credentials? = null) {
    fun requestJson(method: String, path: String, body: JSONObject? = null, readTimeoutMs: Int = READ_TIMEOUT_MS): JSONObject =
        execute(method, path, body, readTimeoutMs) ?: throw ContractException("$method $path returned an empty body")

    fun requestNoContent(method: String, path: String, body: JSONObject? = null) {
        execute(method, path, body, READ_TIMEOUT_MS)
    }

    private fun execute(method: String, path: String, body: JSONObject?, readTimeoutMs: Int): JSONObject? {
        val token = credentials?.accessToken()
        return try {
            send(method, path, body, token, readTimeoutMs)
        } catch (error: ApiException) {
            if (error.status != HttpURLConnection.HTTP_UNAUTHORIZED || credentials == null || token == null) throw error
            credentials.refreshAfterUnauthorized(token)
            send(method, path, body, credentials.accessToken(), readTimeoutMs)
        }
    }

    private fun send(method: String, path: String, body: JSONObject?, token: String?, readTimeoutMs: Int): JSONObject? {
        val connection = (URL(baseUrl.trimEnd('/') + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = readTimeoutMs
            setRequestProperty("Accept", "application/json")
            if (token != null) setRequestProperty("Authorization", "Bearer $token")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
        }
        try {
            if (body != null) connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) throw apiException(status, text)
            return if (text.isEmpty()) null else JSONObject(text)
        } finally {
            connection.disconnect()
        }
    }

    // Badan error bisa bukan JSON (mis. 502 dari proxy), jadi di sini, dan hanya di sini, parsing bersifat longgar.
    private fun apiException(status: Int, text: String): ApiException {
        val json = try {
            JSONObject(text)
        } catch (_: JSONException) {
            null
        }
        val message = json?.takeIf { it.has("error") }?.optString("error") ?: "HTTP $status"
        val code = json?.takeIf { it.has("code") }?.optString("code")
        return ApiException(status, code, message)
    }

    companion object {
        const val CONNECT_TIMEOUT_MS = 10_000
        const val READ_TIMEOUT_MS = 20_000
    }
}

/**
 * PUT ke URL pra-tanda-tangan penyimpanan objek (bukan API Madsaz): tanpa Authorization dan tanpa retry 401.
 * [write] menulis tepat [length] byte ke body.
 */
internal fun putToStorage(url: String, contentType: String, length: Long, readTimeoutMs: Int, write: (OutputStream) -> Unit) {
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        requestMethod = "PUT"
        doOutput = true
        connectTimeout = ApiClient.CONNECT_TIMEOUT_MS
        readTimeout = readTimeoutMs
        setFixedLengthStreamingMode(length)
        setRequestProperty("Content-Type", contentType)
    }
    try {
        connection.outputStream.use(write)
        val status = connection.responseCode
        if (status !in 200..299) throw ApiException(status, "storage_rejected", "Upload rejected with HTTP $status")
    } finally {
        connection.disconnect()
    }
}
