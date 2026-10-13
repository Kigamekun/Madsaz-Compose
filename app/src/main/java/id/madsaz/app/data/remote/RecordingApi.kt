package id.madsaz.app.data.remote

import id.madsaz.app.model.CryScore
import id.madsaz.app.model.RecordingItem
import id.madsaz.app.model.RecordingStatus
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant

class RecordingApi(private val client: ApiClient) {
    /**
     * upload-url -> PUT ke penyimpanan -> complete. Nama file adalah UUID rekaman (lihat RecordingStore).
     * Idempoten di BE: bila rekaman sudah ada, upload-url menjawab already_uploaded dan PUT dilewati.
     * Mengembalikan id rekaman; hasil analisis diambil lewat [get].
     */
    fun upload(file: File, durationSeconds: Int?): String {
        val recordingId = file.nameWithoutExtension
        val target = client.requestJson(
            "POST",
            "/records/upload-url",
            JSONObject().put("recording_id", recordingId).put("size_bytes", file.length()),
        ).obj("data")
        if (!target.has("already_uploaded")) putVideo(target.string("upload_url"), file)

        val complete = JSONObject()
        if (durationSeconds != null) complete.put("duration_seconds", durationSeconds)
        client.requestJson("POST", "/records/$recordingId/complete", complete, readTimeoutMs = COMPLETE_READ_TIMEOUT_MS)
        return recordingId
    }

    fun list(): List<RecordingItem> =
        client.requestJson("GET", "/records").array("data").objects().map(::parseRecording)

    fun get(recordingId: String): RecordingItem =
        parseRecording(client.requestJson("GET", "/records/$recordingId/result").obj("data"))

    fun delete(recordingId: String) {
        client.requestNoContent("DELETE", "/records/$recordingId")
    }

    fun presignedVideoUrl(recordingId: String): String =
        client.requestJson("GET", "/records/$recordingId/video-url").obj("data").string("url")

    fun sendFeedback(recordingId: String, rating: Int, improvement: String, comment: String) {
        client.requestJson(
            "POST",
            "/feedback",
            JSONObject().put("recording_id", recordingId).put("rating", rating).put("improvement", improvement).put("comment", comment),
        )
    }

    // URL pra-tanda-tangan menunjuk ke penyimpanan objek (bukan API), jadi tanpa Authorization dan tanpa retry 401.
    private fun putVideo(uploadUrl: String, file: File) {
        val connection = (URL(uploadUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "PUT"
            doOutput = true
            connectTimeout = ApiClient.CONNECT_TIMEOUT_MS
            readTimeout = UPLOAD_READ_TIMEOUT_MS
            setFixedLengthStreamingMode(file.length())
            setRequestProperty("Content-Type", "video/mp4")
        }
        try {
            connection.outputStream.use { output -> file.inputStream().use { it.copyTo(output) } }
            val status = connection.responseCode
            if (status !in 200..299) throw ApiException(status, "storage_rejected", "Video upload rejected with HTTP $status")
        } finally {
            connection.disconnect()
        }
    }

    private fun parseRecording(json: JSONObject) = RecordingItem(
        id = json.string("id"),
        recordedAtEpochMillis = Instant.parse(json.string("created_at")).toEpochMilli(),
        durationMillis = json.intOrNull("duration_seconds")?.let { it * 1000L },
        sizeBytes = json.long("size_bytes"),
        status = RecordingStatus.fromWire(json.string("status")),
        category = json.stringOrNull("category"),
        solution = json.stringOrNull("solution"),
        source = json.stringOrNull("source"),
        scores = json.arrayOrNull("scores")?.objects()
            ?.map { CryScore(it.string("code"), it.string("label"), it.double("confidence")) }
            .orEmpty(),
    )

    private companion object {
        // /complete mengunduh video dari penyimpanan lalu (mode mock) menganalisis secara sinkron.
        const val COMPLETE_READ_TIMEOUT_MS = 60_000
        const val UPLOAD_READ_TIMEOUT_MS = 60_000
    }
}
