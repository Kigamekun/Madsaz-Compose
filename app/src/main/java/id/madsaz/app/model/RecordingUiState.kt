package id.madsaz.app.model

import androidx.annotation.StringRes

sealed interface RecordingUiState {
    data object PermissionRequired : RecordingUiState
    data object Ready : RecordingUiState
    data class Recording(val elapsedSeconds: Int) : RecordingUiState
    data object Saving : RecordingUiState
    data object Analyzing : RecordingUiState

    /** Video sudah diterima BE; hasil analisisnya diambil dan di-poll oleh ResultViewModel. */
    data class Completed(val recordingId: String) : RecordingUiState

    /** [canRetry] false bila mengulang tidak akan berhasil (mis. kuota gratis habis). */
    data class Error(@StringRes val messageRes: Int, val canRetry: Boolean = true) : RecordingUiState
}
