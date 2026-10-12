package id.madsaz.app.data.local

import android.content.Context
import android.media.MediaMetadataRetriever
import java.io.File
import java.io.IOException
import java.util.UUID

class RecordingStore(context: Context, userId: String) {
    private val directory = File(context.filesDir, "pending-recordings/$userId")
    private val pending = context.getSharedPreferences("pending_recordings", Context.MODE_PRIVATE)
    private val pendingKey = userId

    fun createOutputFile(): File {
        if (pendingUpload() != null) throw IOException("Unggahan sebelumnya belum selesai")
        if (!directory.isDirectory && !directory.mkdirs()) throw IOException("Direktori rekaman gagal dibuat")
        return File(directory, "${UUID.randomUUID()}.mp4")
    }

    fun pendingUpload(): File? {
        val name = pending.getString(pendingKey, null) ?: return null
        val file = File(directory, name)
        if (file.parentFile?.canonicalFile == directory.canonicalFile && file.isFile) return file
        pending.edit().remove(pendingKey).apply()
        return null
    }

    fun markReady(file: File) {
        checkFile(file)
        if (!pending.edit().putString(pendingKey, file.name).commit()) {
            throw IOException("Status unggahan gagal disimpan")
        }
    }

    fun durationSeconds(file: File): Int? {
        checkFile(file)
        return runCatching {
            MediaMetadataRetriever().run {
                try {
                    setDataSource(file.absolutePath)
                    extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        ?.toLongOrNull()?.let { ((it + 999) / 1000).toInt() }
                } finally {
                    release()
                }
            }
        }.getOrNull()
    }

    fun deleteUploaded(file: File) {
        checkFile(file)
        if (!file.delete()) throw IOException("Salinan sementara gagal dihapus")
        pending.edit().remove(pendingKey).apply()
    }

    fun deletePartial(file: File) {
        if (file.parentFile?.canonicalFile == directory.canonicalFile && file.exists()) file.delete()
    }

    private fun checkFile(file: File) {
        require(file.parentFile?.canonicalFile == directory.canonicalFile && file.isFile) {
            "File rekaman sementara tidak ditemukan"
        }
    }
}
