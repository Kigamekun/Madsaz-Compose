package id.madsaz.app.feature.recording

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.madsaz.app.R
import id.madsaz.app.data.local.RecordingStore
import id.madsaz.app.model.RecordingUiState
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RecordingViewModel(private val store: RecordingStore) : ViewModel() {
    var uiState: RecordingUiState by mutableStateOf(RecordingUiState.PermissionRequired)
        private set

    private var analysisJob: Job? = null

    fun onPermissionState(granted: Boolean) {
        if (uiState is RecordingUiState.PermissionRequired || uiState is RecordingUiState.Ready) {
            uiState = if (granted) RecordingUiState.Ready else RecordingUiState.PermissionRequired
        }
    }

    fun createOutputFile(): File = store.createOutputFile()

    fun onRecordingStarted() {
        uiState = RecordingUiState.Recording(elapsedSeconds = 0)
    }

    fun onRecordingProgress(recordedDurationNanos: Long) {
        val current = uiState as? RecordingUiState.Recording ?: return
        val elapsedSeconds = (recordedDurationNanos / NANOS_PER_SECOND).toInt().coerceAtMost(MAX_SECONDS)
        if (elapsedSeconds != current.elapsedSeconds) {
            uiState = RecordingUiState.Recording(elapsedSeconds)
        }
    }

    fun onSaving() {
        if (uiState is RecordingUiState.Recording) uiState = RecordingUiState.Saving
    }

    fun onRecordingFinalized(file: File) {
        analysisJob?.cancel()
        analysisJob = viewModelScope.launch {
            uiState = RecordingUiState.Analyzing
            delay(ANALYSIS_DELAY_MILLIS)
            uiState = try {
                RecordingUiState.Completed(store.recordingFrom(file))
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                RecordingUiState.Error(R.string.recording_save_error)
            }
        }
    }

    fun onRecordingFailed(file: File) {
        analysisJob?.cancel()
        viewModelScope.launch {
            try {
                store.deletePartial(file)
            } finally {
                uiState = RecordingUiState.Error(R.string.recording_save_error)
            }
        }
    }

    fun onCameraError() {
        uiState = RecordingUiState.Error(R.string.recording_camera_error)
    }

    fun reset(permissionsGranted: Boolean) {
        analysisJob?.cancel()
        uiState = if (permissionsGranted) RecordingUiState.Ready else RecordingUiState.PermissionRequired
    }

    private companion object {
        const val MAX_SECONDS = 30
        const val NANOS_PER_SECOND = 1_000_000_000L
        const val ANALYSIS_DELAY_MILLIS = 2_000L
    }
}
