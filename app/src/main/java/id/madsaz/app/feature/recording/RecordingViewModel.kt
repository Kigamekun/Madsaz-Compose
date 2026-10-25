package id.madsaz.app.feature.recording

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.madsaz.app.R
import id.madsaz.app.data.local.RecordingStore
import id.madsaz.app.data.remote.ApiException
import id.madsaz.app.model.RecordingUiState
import id.madsaz.app.ui.userMessageRes
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val MAX_RECORDING_SECONDS = 5

/**
 * [upload] mengirim [File] (dan durasinya) lalu mengembalikan id hasil: akun memakai `RecordingApi.upload`, Coba Gratis memakai
 * `GuestApi.analyze`. Keduanya menolak dengan kode batas harian yang sama-sama berarti "jangan ulangi".
 */
class RecordingViewModel(
    private val store: RecordingStore,
    private val upload: (File, Int?) -> String,
) : ViewModel() {
    var uiState: RecordingUiState by mutableStateOf(RecordingUiState.PermissionRequired)
        private set

    private var uploadJob: Job? = null
    private var pendingFile: File? = store.pendingUpload()
    val hasPendingUpload: Boolean get() = pendingFile != null

    init {
        if (pendingFile != null) uiState = RecordingUiState.Error(R.string.recording_upload_error)
    }

    fun onPermissionState(granted: Boolean) {
        if (uiState is RecordingUiState.PermissionRequired || uiState is RecordingUiState.Ready) {
            uiState = if (granted) RecordingUiState.Ready else RecordingUiState.PermissionRequired
        }
    }

    /** null bila file rekaman tidak bisa dibuat; state sudah menjadi Error dengan pesan penyimpanan. */
    fun createOutputFile(): File? = try {
        store.createOutputFile()
    } catch (_: IOException) {
        uiState = RecordingUiState.Error(R.string.recording_save_error)
        null
    }

    fun onRecordingStarted() { uiState = RecordingUiState.Recording(0) }

    fun onRecordingProgress(recordedDurationNanos: Long) {
        val current = uiState as? RecordingUiState.Recording ?: return
        val seconds = (recordedDurationNanos / 1_000_000_000L).toInt().coerceAtMost(MAX_RECORDING_SECONDS)
        if (seconds != current.elapsedSeconds) uiState = RecordingUiState.Recording(seconds)
    }

    fun onSaving() {
        if (uiState is RecordingUiState.Recording) uiState = RecordingUiState.Saving
    }

    fun onRecordingFinalized(file: File) {
        try {
            store.markReady(file)
        } catch (_: IOException) {
            uiState = RecordingUiState.Error(R.string.recording_save_error)
            return
        }
        pendingFile = file
        retryUpload()
    }

    fun retryUpload() {
        val file = pendingFile ?: return
        uploadJob?.cancel()
        uploadJob = viewModelScope.launch {
            uiState = RecordingUiState.Analyzing
            uiState = try {
                val recordingId = withContext(Dispatchers.IO) { upload(file, store.durationSeconds(file)) }
                try {
                    withContext(Dispatchers.IO) { store.delete(file) }
                    pendingFile = null
                } catch (_: IOException) {
                    // Video sudah di server. Salinan lokal dibersihkan pada percobaan berikutnya (BE menjawab already_uploaded).
                }
                RecordingUiState.Completed(recordingId)
            } catch (error: CancellationException) {
                throw error
            } catch (error: ApiException) {
                if (error.code in LIMIT_CODES) {
                    // Mengulang tidak akan berhasil dan BE sudah membuang videonya; jangan biarkan file menghalangi rekaman baru.
                    withContext(Dispatchers.IO) { store.delete(file) }
                    pendingFile = null
                    RecordingUiState.Error(error.userMessageRes(), canRetry = false)
                } else {
                    RecordingUiState.Error(error.userMessageRes())
                }
            } catch (error: Exception) {
                RecordingUiState.Error(error.userMessageRes())
            }
        }
    }

    fun onRecordingFailed(file: File) {
        uploadJob?.cancel()
        viewModelScope.launch {
            withContext(Dispatchers.IO) { store.deletePartial(file) }
            uiState = RecordingUiState.Error(R.string.recording_save_error)
        }
    }

    fun onCameraError() { uiState = RecordingUiState.Error(R.string.recording_camera_error) }

    /** Membuang rekaman yang belum terkirim agar pengguna bisa merekam ulang. */
    fun discardPending(permissionsGranted: Boolean) {
        uploadJob?.cancel()
        viewModelScope.launch {
            pendingFile?.let { withContext(Dispatchers.IO) { store.delete(it) } }
            pendingFile = null
            reset(permissionsGranted)
        }
    }

    fun reset(permissionsGranted: Boolean) {
        uploadJob?.cancel()
        uiState = if (permissionsGranted) RecordingUiState.Ready else RecordingUiState.PermissionRequired
    }

    private companion object {
        val LIMIT_CODES = setOf("free_limit_reached", "guest_limit_reached")
    }
}
