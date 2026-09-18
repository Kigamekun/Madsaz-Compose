package id.madsaz.app.data.local

import android.content.Context
import android.media.MediaMetadataRetriever
import id.madsaz.app.model.DemoClassifier
import id.madsaz.app.model.RecordingItem
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecordingStore(context: Context) {
    private val directory = File(context.filesDir, DIRECTORY_NAME)

    fun createOutputFile(): File {
        ensureDirectory()
        return File(directory, "$FILE_PREFIX${System.currentTimeMillis()}.$FILE_EXTENSION")
    }

    suspend fun listRecordings(): List<RecordingItem> = withContext(Dispatchers.IO) {
        ensureDirectory()
        val files = directory.listFiles { file ->
            file.isFile && file.extension == FILE_EXTENSION && ID_PATTERN.matches(file.nameWithoutExtension)
        } ?: throw IOException("Direktori rekaman tidak dapat dibaca")

        files.map(::toRecordingItem).sortedByDescending(RecordingItem::recordedAtEpochMillis)
    }

    suspend fun recordingFrom(file: File): RecordingItem = withContext(Dispatchers.IO) {
        require(file.parentFile?.canonicalFile == directory.canonicalFile) {
            "File berada di luar direktori rekaman"
        }
        require(file.isFile) { "File rekaman tidak ditemukan" }
        toRecordingItem(file)
    }

    suspend fun delete(recording: RecordingItem) = withContext(Dispatchers.IO) {
        val file = File(recording.filePath)
        require(file.parentFile?.canonicalFile == directory.canonicalFile) {
            "File berada di luar direktori rekaman"
        }
        if (file.exists() && !file.delete()) throw IOException("Rekaman gagal dihapus")
    }

    suspend fun deletePartial(file: File) = withContext(Dispatchers.IO) {
        if (file.parentFile?.canonicalFile == directory.canonicalFile && file.exists()) {
            file.delete()
        }
    }

    private fun ensureDirectory() {
        if (!directory.isDirectory && !directory.mkdirs()) {
            throw IOException("Direktori rekaman gagal dibuat")
        }
    }

    private fun toRecordingItem(file: File): RecordingItem {
        val id = file.nameWithoutExtension
        val recordedAt = id.removePrefix(FILE_PREFIX).toLongOrNull()
            ?: throw IOException("Nama file rekaman tidak valid")
        return RecordingItem(
            id = id,
            filePath = file.absolutePath,
            recordedAtEpochMillis = recordedAt,
            durationMillis = readDuration(file),
            sizeBytes = file.length(),
            category = DemoClassifier.classify(id),
        )
    }

    private fun readDuration(file: File): Long? = runCatching {
        MediaMetadataRetriever().use { retriever ->
            retriever.setDataSource(file.absolutePath)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
        }
    }.getOrNull()

    private inline fun <T> MediaMetadataRetriever.use(block: (MediaMetadataRetriever) -> T): T =
        try {
            block(this)
        } finally {
            release()
        }

    private companion object {
        const val DIRECTORY_NAME = "recordings"
        const val FILE_PREFIX = "madsaz_"
        const val FILE_EXTENSION = "mp4"
        val ID_PATTERN = Regex("^madsaz_\\d+$")
    }
}
