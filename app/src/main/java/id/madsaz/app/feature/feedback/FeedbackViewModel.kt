package id.madsaz.app.feature.feedback

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.madsaz.app.R
import id.madsaz.app.data.remote.RecordingApi
import id.madsaz.app.ui.userMessageRes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Nilai kawat yang diwajibkan BE (feedback.js); label tampilannya boleh dilokalkan tanpa merusak API. */
enum class FeedbackTopic(val wire: String, @StringRes val labelRes: Int) {
    ACCURACY("Accuracy", R.string.feedback_topic_accuracy),
    USABILITY("Usability", R.string.feedback_topic_usability),
    OTHER("Something else", R.string.feedback_topic_other),
}

sealed interface FeedbackUiState {
    data object Idle : FeedbackUiState
    data object Submitting : FeedbackUiState
    data class Error(@StringRes val messageRes: Int) : FeedbackUiState
    data object Sent : FeedbackUiState
}

class FeedbackViewModel(
    private val recordingId: String,
    private val api: RecordingApi,
) : ViewModel() {
    var uiState: FeedbackUiState by mutableStateOf(FeedbackUiState.Idle)
        private set

    fun submit(rating: Int, topic: FeedbackTopic, note: String) {
        if (uiState is FeedbackUiState.Submitting) return
        uiState = FeedbackUiState.Submitting
        viewModelScope.launch {
            uiState = try {
                withContext(Dispatchers.IO) { api.sendFeedback(recordingId, rating, topic.wire, note.trim()) }
                FeedbackUiState.Sent
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                FeedbackUiState.Error(error.userMessageRes())
            }
        }
    }
}
