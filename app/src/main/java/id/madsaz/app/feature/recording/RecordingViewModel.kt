package id.madsaz.app.feature.recording

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.madsaz.app.R
import id.madsaz.app.data.local.RecordingStore
import id.madsaz.app.data.remote.RecordingApi
import id.madsaz.app.model.RecordingUiState
import java.io.File
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecordingViewModel(
    private val store: RecordingStore,
    private val accessToken: String,
    private val api: RecordingApi = RecordingApi(),
) : ViewModel() {
    var uiState: RecordingUiState by mutableStateOf(RecordingUiState.PermissionRequired)
        private set

    private var uploadJob: Job? = null
    private var pendingFile: File? = store.pendingUpload()
    private var serverId: String? = null
    val hasPendingUpload: Boolean get() = pendingFile != null

    init {
        if (pendingFile != null) uiState = RecordingUiState.Error(R.string.recording_upload_error)
    }

    fun onPermissionState(granted: Boolean) {
        if (uiState is RecordingUiState.PermissionRequired || uiState is RecordingUiState.Ready) {
            uiState = if (granted) RecordingUiState.Ready else RecordingUiState.PermissionRequired
        }
    }

    fun createOutputFile(): File = store.createOutputFile()

    fun onRecordingStarted() { uiState = RecordingUiState.Recording(0) }

    fun onRecordingProgress(recordedDurationNanos: Long) {
        val current = uiState as? RecordingUiState.Recording ?: return
        val seconds = (recordedDurationNanos / 1_000_000_000L).toInt().coerceAtMost(30)
        if (seconds != current.elapsedSeconds) uiState = RecordingUiState.Recording(seconds)
    }

    fun onSaving() {
        if (uiState is RecordingUiState.Recording) uiState = RecordingUiState.Saving
    }

    fun onRecordingFinalized(file: File) {
        try {
            store.markReady(file)
            pendingFile = file
            serverId = null
            retryUpload()
        } catch (_: Exception) {
            uiState = RecordingUiState.Error(R.string.recording_save_error)
        }
    }

    fun retryUpload() {
        val file = pendingFile ?: return
        uploadJob?.cancel()
        uploadJob = viewModelScope.launch {
            uiState = RecordingUiState.Analyzing
            uiState = try {
                val recording = withContext(Dispatchers.IO) {
                    val id = serverId ?: api.upload(accessToken, file, store.durationSeconds(file)).also { serverId = it }
                    api.get(accessToken, id).also { if (file.exists()) store.deleteUploaded(file) }
                }
                pendingFile = null
                RecordingUiState.Completed(recording)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.e("MadsazUpload", "Recording upload failed", error)
                RecordingUiState.Error(R.string.recording_upload_error, error.message)
            }
        }
    }

    fun onRecordingFailed(file: File) {
        uploadJob?.cancel()
        viewModelScope.launch {
            try {
                store.deletePartial(file)
            } finally {
                uiState = RecordingUiState.Error(R.string.recording_save_error)
            }
        }
    }

    fun onCameraError() { uiState = RecordingUiState.Error(R.string.recording_camera_error) }

    fun reset(permissionsGranted: Boolean) {
        uploadJob?.cancel()
        uiState = if (permissionsGranted) RecordingUiState.Ready else RecordingUiState.PermissionRequired
    }
}
