package id.madsaz.app.data.remote

import id.madsaz.app.model.CryScore
import org.json.JSONObject
import java.io.File

/** Hasil analisis Coba Gratis: tidak disimpan di server maupun di perangkat. */
data class GuestResult(val scores: List<CryScore>)

/** Coba Gratis tanpa akun; [client] tanpa token. Video diunggah, dianalisis sekali, lalu dihapus BE. */
class GuestApi(private val client: ApiClient) {
    /** [deviceId] = `GuestDevice.id()`; BE membatasi analisis per perangkat dan per IP (429 `guest_limit_reached`). */
    fun analyze(file: File, deviceId: String): GuestResult {
        val slot = client.requestJson(
            "POST", "/guest/upload-url",
            JSONObject().put("device_id", deviceId).put("size_bytes", file.length()),
        ).obj("data")
        putToStorage(slot.string("upload_url"), "video/mp4", file.length(), READ_TIMEOUT_MS) { output ->
            file.inputStream().use { it.copyTo(output) }
        }
        val result = client.requestJson(
            "POST", "/guest/${slot.string("analysis_id")}/analyze",
            JSONObject().put("device_id", deviceId),
            readTimeoutMs = READ_TIMEOUT_MS,
        ).obj("data")
        return GuestResult(result.array("scores").objects().map { CryScore(it.string("code"), it.string("label"), it.double("confidence")) })
    }

    private companion object {
        // Analisis BE sinkron dan bisa memakan sampai 60 detik (ANALYSIS_TIMEOUT_MS di BE).
        const val READ_TIMEOUT_MS = 70_000
    }
}
