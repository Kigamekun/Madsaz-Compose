package id.madsaz.app.data.remote

import id.madsaz.app.BuildConfig
import id.madsaz.app.model.RecordingItem
import id.madsaz.app.model.CryScore
import android.media.MediaMetadataRetriever
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import org.json.JSONObject

class RecordingApi {
    fun upload(accessToken: String, file: File, durationSeconds: Int?): String {
        val dimensions = videoDimensions(file)
        val recordingId = file.nameWithoutExtension
        val request = JSONObject().put("recording_id", recordingId).put("size_bytes", file.length())
        val uploadData = requestJson("/records/upload-url", accessToken, "POST", request).getJSONObject("data")
        if (uploadData.optBoolean("already_uploaded")) return recordingId
        val uploadUrl = uploadData.getString("upload_url")
        val upload = (URL(uploadUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "PUT"
            doOutput = true
            connectTimeout = 10_000
            readTimeout = 60_000
            setFixedLengthStreamingMode(file.length())
            setRequestProperty("Content-Type", "video/mp4")
        }
        try {
            upload.outputStream.use { output -> file.inputStream().use { it.copyTo(output) } }
            if (upload.responseCode !in 200..299) throw IllegalStateException("Unggah MinIO gagal (${upload.responseCode})")
        } finally {
            upload.disconnect()
        }
        val complete = JSONObject()
        if (durationSeconds != null) complete.put("duration_seconds", durationSeconds)
        dimensions?.let { (width, height) -> complete.put("video_width", width).put("video_height", height) }
        requestJson("/records/$recordingId/complete", accessToken, "POST", complete)
        return recordingId
    }

    private fun requestJson(path: String, accessToken: String, method: String, body: JSONObject): JSONObject {
        val connection = connection(path, accessToken).apply {
            requestMethod = method
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            readTimeout = 60_000
        }
        try {
            connection.outputStream.use { it.write(body.toString().toByteArray()) }
            return response(connection)
        } finally {
            connection.disconnect()
        }
    }

    fun list(accessToken: String): List<RecordingItem> {
        val connection = connection("/records", accessToken)
        try {
            val data = response(connection).getJSONArray("data")
            return (0 until data.length()).map { parseRecording(data.getJSONObject(it)) }
        } finally {
            connection.disconnect()
        }
    }

    fun get(accessToken: String, recordingId: String): RecordingItem {
        val connection = connection("/records/$recordingId/result", accessToken)
        try {
            return parseRecording(response(connection).getJSONObject("data"))
        } finally {
            connection.disconnect()
        }
    }

    fun presignedVideoUrl(accessToken: String, recordingId: String): String {
        val connection = connection("/records/$recordingId/video-url", accessToken)
        try {
            return response(connection).getJSONObject("data").getString("url")
        } finally {
            connection.disconnect()
        }
    }

    private fun parseRecording(json: JSONObject) = RecordingItem(
        id = json.getString("id"),
        recordedAtEpochMillis = Instant.parse(json.getString("created_at")).toEpochMilli(),
        durationMillis = if (json.isNull("duration_seconds")) null else json.getLong("duration_seconds") * 1000,
        sizeBytes = json.optLong("size_bytes"),
        status = json.getString("status"),
        category = if (json.isNull("category")) null else json.getString("category").ifBlank { null },
        confidence = if (json.isNull("confidence")) null else json.getDouble("confidence"),
        solution = if (json.isNull("solution")) null else json.getString("solution").ifBlank { null },
        source = if (json.isNull("source")) null else json.getString("source"),
        scores = json.optJSONArray("scores")?.let { scores ->
            (0 until scores.length()).mapNotNull { index ->
                scores.optJSONObject(index)?.let { score ->
                    val code = score.optString("code")
                    if (code.isBlank() || score.isNull("confidence")) null
                    else CryScore(code, score.optString("label", code), score.optDouble("confidence"))
                }
            }
        }.orEmpty(),
        videoWidth = if (json.isNull("video_width")) null else json.getInt("video_width"),
        videoHeight = if (json.isNull("video_height")) null else json.getInt("video_height"),
    )

    private fun videoDimensions(file: File): Pair<Int, Int>? = runCatching {
        MediaMetadataRetriever().let { retriever ->
            try {
                retriever.setDataSource(file.absolutePath)
                val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: return null
                val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: return null
                if (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() in listOf(90, 270)) {
                    height to width
                } else {
                    width to height
                }
            } finally {
                retriever.release()
            }
        }
    }.getOrNull()

    fun sendFeedback(accessToken: String, recordingId: String, rating: Int, improvement: String, comment: String) {
        val connection = connection("/feedback", accessToken).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
        }
        try {
            val body = JSONObject()
                .put("recording_id", recordingId)
                .put("rating", rating)
                .put("improvement", improvement)
                .put("comment", comment)
            connection.outputStream.use { it.write(body.toString().toByteArray()) }
            response(connection)
        } finally {
            connection.disconnect()
        }
    }

    private fun connection(path: String, accessToken: String) =
        (URL(BuildConfig.API_BASE_URL.trimEnd('/') + path).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 20_000
            setRequestProperty("Authorization", "Bearer $accessToken")
            setRequestProperty("Accept", "application/json")
        }

    private fun response(connection: HttpURLConnection): JSONObject {
        val status = connection.responseCode
        val body = (if (status in 200..299) connection.inputStream else connection.errorStream)
            ?.bufferedReader()?.use { it.readText() }.orEmpty()
        if (status !in 200..299) {
            val message = runCatching { JSONObject(body).optString("error") }.getOrNull()
            throw IllegalStateException(message?.ifBlank { null } ?: "Permintaan gagal ($status)")
        }
        return JSONObject(body)
    }
}
