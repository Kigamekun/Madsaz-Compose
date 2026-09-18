package id.madsaz.app.model

data class RecordingItem(
    val id: String,
    val filePath: String,
    val recordedAtEpochMillis: Long,
    val durationMillis: Long?,
    val sizeBytes: Long,
    val category: CryCategory,
)
