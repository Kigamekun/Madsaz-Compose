package id.madsaz.app.feature.history

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.madsaz.app.R
import id.madsaz.app.data.local.RecordingStore
import id.madsaz.app.model.RecordingItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data class Content(val recordings: List<RecordingItem>) : HistoryUiState {
        val totalBytes: Long = recordings.sumOf(RecordingItem::sizeBytes)
    }
    data class Error(@StringRes val messageRes: Int) : HistoryUiState
}

class HistoryViewModel(private val store: RecordingStore) : ViewModel() {
    var uiState: HistoryUiState by mutableStateOf(HistoryUiState.Loading)
        private set

    private var loadJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            uiState = HistoryUiState.Loading
            uiState = try {
                HistoryUiState.Content(store.listRecordings())
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                HistoryUiState.Error(R.string.history_load_error)
            }
        }
    }

    fun delete(recording: RecordingItem) {
        viewModelScope.launch {
            try {
                store.delete(recording)
                refresh()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                uiState = HistoryUiState.Error(R.string.history_load_error)
            }
        }
    }
}
