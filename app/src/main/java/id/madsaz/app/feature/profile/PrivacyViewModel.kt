package id.madsaz.app.feature.profile

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.madsaz.app.data.remote.PrivacyPolicy
import id.madsaz.app.ui.userMessageRes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface PrivacyUiState {
    data object Loading : PrivacyUiState
    data class Content(val policy: PrivacyPolicy) : PrivacyUiState
    data class Error(@StringRes val messageRes: Int) : PrivacyUiState
}

/** [fetch] memuat satu dokumen hukum (kebijakan privasi atau S&K) dalam bahasa yang dipilih pemanggil. */
class PrivacyViewModel(private val fetch: () -> PrivacyPolicy) : ViewModel() {
    var uiState: PrivacyUiState by mutableStateOf(PrivacyUiState.Loading)
        private set

    init { load() }

    fun load() {
        viewModelScope.launch {
            uiState = PrivacyUiState.Loading
            uiState = try {
                PrivacyUiState.Content(withContext(Dispatchers.IO) { fetch() })
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                PrivacyUiState.Error(error.userMessageRes())
            }
        }
    }
}
