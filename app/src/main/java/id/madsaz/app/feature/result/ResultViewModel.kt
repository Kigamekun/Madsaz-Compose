package id.madsaz.app.feature.result

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.madsaz.app.data.remote.RecordingApi
import id.madsaz.app.model.RecordingItem
import id.madsaz.app.model.RecordingStatus
import id.madsaz.app.ui.userMessageRes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface ResultUiState {
    data object Loading : ResultUiState
    data class Content(val recording: RecordingItem) : ResultUiState
    data class Error(@StringRes val messageRes: Int) : ResultUiState
}

/**
 * Hasil analisis satu rekaman. BE menganalisis secara asinkron (complete menjawab 202 dengan status processing),
 * jadi hasilnya di-poll sampai selesai atau gagal. Batas poll sedikit di atas batas analisis BE (60 detik).
 */
class ResultViewModel(
    private val recordingId: String,
    private val api: RecordingApi,
    private val pollIntervalMs: Long = POLL_INTERVAL_MS,
) : ViewModel() {
    var uiState: ResultUiState by mutableStateOf(ResultUiState.Loading)
        private set

    private var job: Job? = null

    init { load() }

    /** URL pra-tanda-tangan (berlaku 5 menit) untuk memutar video rekaman ini. */
    suspend fun videoUrl(): String = withContext(Dispatchers.IO) { api.presignedVideoUrl(recordingId) }

    /** Memuat ulang dan melanjutkan polling; dipakai juga oleh tombol "Perbarui hasil". */
    fun load() {
        job?.cancel()
        job = viewModelScope.launch {
            if (uiState is ResultUiState.Error) uiState = ResultUiState.Loading
            repeat(MAX_POLLS) { attempt ->
                val recording = try {
                    withContext(Dispatchers.IO) { api.get(recordingId) }
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    uiState = ResultUiState.Error(error.userMessageRes())
                    return@launch
                }
                uiState = ResultUiState.Content(recording)
                if (recording.status != RecordingStatus.PROCESSING) return@launch
                if (attempt < MAX_POLLS - 1) delay(pollIntervalMs)
            }
        }
    }

    private companion object {
        const val POLL_INTERVAL_MS = 2_000L
        const val MAX_POLLS = 45
    }
}
