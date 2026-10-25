package id.madsaz.app.feature.auth

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.madsaz.app.R
import id.madsaz.app.data.remote.AuthApi
import id.madsaz.app.ui.userMessageRes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface ForgotPasswordState {
    data object Idle : ForgotPasswordState
    data object Submitting : ForgotPasswordState
    data object Sent : ForgotPasswordState
    data class Error(@StringRes val messageRes: Int) : ForgotPasswordState
}

class ForgotPasswordViewModel(private val authApi: AuthApi, private val language: String) : ViewModel() {
    var state: ForgotPasswordState by mutableStateOf(ForgotPasswordState.Idle)
        private set

    fun send(email: String) {
        if (state is ForgotPasswordState.Submitting) return
        if (email.isBlank()) {
            state = ForgotPasswordState.Error(R.string.error_email_required)
            return
        }
        viewModelScope.launch {
            state = ForgotPasswordState.Submitting
            state = try {
                withContext(Dispatchers.IO) { authApi.forgotPassword(email.trim(), language) }
                ForgotPasswordState.Sent
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                ForgotPasswordState.Error(error.userMessageRes())
            }
        }
    }
}
