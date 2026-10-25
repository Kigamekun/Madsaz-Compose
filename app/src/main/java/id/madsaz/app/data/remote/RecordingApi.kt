package id.madsaz.app.data.remote

import android.media.MediaMetadataRetriever
import id.madsaz.app.model.CryScore
import id.madsaz.app.model.RecordingItem
import id.madsaz.app.model.RecordingStatus
import org.json.JSONObject
import java.io.File
import java.time.Instant

class RecordingApi(
    private val client: ApiClient,
    /** Ukuran tampilan video (lebar, tinggi); parameter agar bisa diganti di JVM test, yang tidak punya MediaMetadataRetriever. */
    private val videoDimensions: (File) -> Pair<Int, Int>? = ::readVideoDimensions,
) {
    /**
     * upload-url -> PUT ke penyimpanan -> complete. Nama file adalah UUID rekaman (lihat RecordingStore).
     * Idempoten di BE: bila rekaman sudah ada, upload-url menjawab already_uploaded dan PUT dilewati.
     * Mengembalikan id rekaman; hasil analisis diambil lewat [get].
     */
    fun upload(file: File, durationSeconds: Int?): String {
        val recordingId = file.nameWithoutExtension
        val dimensions = videoDimensions(file)
        val target = client.requestJson(
            "POST",
            "/records/upload-url",
            JSONObject().put("recording_id", recordingId).put("size_bytes", file.length()),
        ).obj("data")
        if (!target.has("already_uploaded")) {
            putToStorage(target.string("upload_url"), "video/mp4", file.length(), UPLOAD_READ_TIMEOUT_MS) { output ->
                file.inputStream().use { it.copyTo(output) }
            }
        }

        val complete = JSONObject()
        if (durationSeconds != null) complete.put("duration_seconds", durationSeconds)
        dimensions?.let { (width, height) -> complete.put("video_width", width).put("video_height", height) }
        client.requestJson("POST", "/records/$recordingId/complete", complete, readTimeoutMs = COMPLETE_READ_TIMEOUT_MS)
        return recordingId
    }

    fun list(): List<RecordingItem> =
        client.requestJson("GET", "/records").array("data").objects().map(::parseRecording)

    fun get(recordingId: String): RecordingItem =
        parseRecording(client.requestJson("GET", "/records/$recordingId/result").obj("data"))

    fun presignedVideoUrl(recordingId: String): String =
        client.requestJson("GET", "/records/$recordingId/video-url").obj("data").string("url")

    fun sendFeedback(recordingId: String, rating: Int, improvement: String?, comment: String) {
        client.requestJson(
            "POST",
            "/feedback",
            JSONObject().put("recording_id", recordingId).put("rating", rating).put("improvement", improvement).put("comment", comment),
        )
    }

    private fun parseRecording(json: JSONObject) = RecordingItem(
        id = json.string("id"),
        recordedAtEpochMillis = Instant.parse(json.string("created_at")).toEpochMilli(),
        durationMillis = json.intOrNull("duration_seconds")?.let { it * 1000L },
        sizeBytes = json.long("size_bytes"),
        status = RecordingStatus.fromWire(json.string("status")),
        category = json.stringOrNull("category"),
        source = json.stringOrNull("source"),
        scores = json.arrayOrNull("scores")?.objects()
            ?.map { CryScore(it.string("code"), it.string("label"), it.double("confidence")) }
            .orEmpty(),
        videoWidth = json.intOrNull("video_width"),
        videoHeight = json.intOrNull("video_height"),
    )

    private companion object {
        // /complete mengunduh video dari penyimpanan lalu (mode mock) menganalisis secara sinkron.
        const val COMPLETE_READ_TIMEOUT_MS = 60_000
        const val UPLOAD_READ_TIMEOUT_MS = 60_000
    }
}

// Ukuran tampilan video (sudah memperhitungkan rotasi) dibaca dari file sebelum diunggah. Opsional di BE:
// file yang tidak bisa dibaca tetap diunggah tanpa ukuran.
private fun readVideoDimensions(file: File): Pair<Int, Int>? {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(file.absolutePath)
        fun metadata(key: Int) = retriever.extractMetadata(key)?.toIntOrNull()
        val width = metadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH) ?: return null
        val height = metadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT) ?: return null
        if (metadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION) in listOf(90, 270)) height to width else width to height
    } catch (_: RuntimeException) {
        null
    } finally {
        retriever.release()
    }
}
