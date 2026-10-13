package id.madsaz.app.model

import androidx.annotation.StringRes

sealed interface RecordingUiState {
    data object PermissionRequired : RecordingUiState
    data object Ready : RecordingUiState
    data class Recording(val elapsedSeconds: Int) : RecordingUiState
    data object Saving : RecordingUiState
    data object Analyzing : RecordingUiState
    data class Completed(val recording: RecordingItem) : RecordingUiState
    data class Error(@StringRes val messageRes: Int, val detail: String? = null) : RecordingUiState
}
