package id.madsaz.app.feature.history

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.madsaz.app.data.remote.RecordingApi
import id.madsaz.app.model.RecordingItem
import id.madsaz.app.ui.userMessageRes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface HistoryUiState {
    data object Loading : HistoryUiState

    /** [noticeRes] adalah kegagalan sesaat (refresh atau hapus) yang tidak boleh menghapus daftar yang sudah tampil. */
    data class Content(val recordings: List<RecordingItem>, @StringRes val noticeRes: Int? = null) : HistoryUiState {
        val totalBytes: Long = recordings.sumOf(RecordingItem::sizeBytes)
    }

    data class Error(@StringRes val messageRes: Int) : HistoryUiState
}

class HistoryViewModel(private val api: RecordingApi) : ViewModel() {
    var uiState: HistoryUiState by mutableStateOf(HistoryUiState.Loading)
        private set

    private var loadJob: Job? = null

    init { refresh() }

    fun refresh() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val shown = uiState as? HistoryUiState.Content
            if (shown == null) uiState = HistoryUiState.Loading
            uiState = try {
                HistoryUiState.Content(withContext(Dispatchers.IO) { api.list() })
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                shown?.copy(noticeRes = error.userMessageRes()) ?: HistoryUiState.Error(error.userMessageRes())
            }
        }
    }

    fun delete(recording: RecordingItem) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { api.delete(recording.id) }
                refresh()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                val shown = uiState as? HistoryUiState.Content ?: return@launch
                uiState = shown.copy(noticeRes = error.userMessageRes())
            }
        }
    }

    fun dismissNotice() {
        val shown = uiState as? HistoryUiState.Content ?: return
        if (shown.noticeRes != null) uiState = shown.copy(noticeRes = null)
    }
}
