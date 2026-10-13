package id.madsaz.app.model

data class CryScore(val code: String, val label: String, val confidence: Double)

data class RecordingItem(
    val id: String,
    val recordedAtEpochMillis: Long,
    val durationMillis: Long?,
    val sizeBytes: Long,
    val status: String,
    val category: String?,
    val confidence: Double?,
    val solution: String?,
    val source: String? = null,
    val scores: List<CryScore> = emptyList(),
    val videoWidth: Int? = null,
    val videoHeight: Int? = null,
)
