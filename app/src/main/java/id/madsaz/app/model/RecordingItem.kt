package id.madsaz.app.model

import androidx.annotation.StringRes
import id.madsaz.app.R

data class CryScore(val code: String, val label: String, val confidence: Double)

/** Nilai `status` dari BE (CHECK constraint di db/init.sql). */
enum class RecordingStatus(val wire: String, @StringRes val labelRes: Int) {
    PROCESSING("processing", R.string.status_processing),
    COMPLETED("completed", R.string.status_completed),
    FAILED("failed", R.string.status_failed);

    companion object {
        fun fromWire(value: String): RecordingStatus =
            entries.firstOrNull { it.wire == value } ?: error("Unknown recording status '$value'")
    }
}

data class RecordingItem(
    val id: String,
    val recordedAtEpochMillis: Long,
    val durationMillis: Long?,
    val sizeBytes: Long,
    val status: RecordingStatus,
    val category: String?,
    val solution: String?,
    val source: String?,
    /** Kosong selama BE belum punya hasil analisis (scores null). */
    val scores: List<CryScore>,
    /** Ukuran tampilan video setelah rotasi; null untuk rekaman tanpa ukuran. */
    val videoWidth: Int?,
    val videoHeight: Int?,
)
