package id.madsaz.app.feature.profile

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.madsaz.app.R
import id.madsaz.app.data.remote.ApiException
import id.madsaz.app.data.remote.ProfileApi
import id.madsaz.app.data.remote.SessionManager
import id.madsaz.app.ui.userMessageRes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface ChangePasswordState {
    data object Idle : ChangePasswordState
    data object Submitting : ChangePasswordState
    data object Done : ChangePasswordState
    data class Error(@StringRes val messageRes: Int) : ChangePasswordState
}

class ChangePasswordViewModel(
    private val api: ProfileApi,
    private val sessionManager: SessionManager,
) : ViewModel() {
    var state: ChangePasswordState by mutableStateOf(ChangePasswordState.Idle)
        private set

    fun reset() { state = ChangePasswordState.Idle }

    fun submit(current: String, new: String, confirm: String) {
        if (state is ChangePasswordState.Submitting) return
        state = when {
            current.isEmpty() || new.isEmpty() -> ChangePasswordState.Error(R.string.error_invalid_input)
            new.length < MIN_PASSWORD_LENGTH -> ChangePasswordState.Error(R.string.error_password_short)
            new != confirm -> ChangePasswordState.Error(R.string.error_password_mismatch)
            else -> {
                viewModelScope.launch {
                    state = ChangePasswordState.Submitting
                    state = try {
                        // BE mencabut sesi lain dan memberi sesi baru untuk perangkat ini; token lama sudah tidak berlaku.
                        val session = withContext(Dispatchers.IO) { api.changePassword(current, new) }
                        sessionManager.signIn(session)
                        ChangePasswordState.Done
                    } catch (error: CancellationException) {
                        throw error
                    } catch (error: ApiException) {
                        // Pada endpoint ini invalid_credentials hanya berarti kata sandi lama salah.
                        ChangePasswordState.Error(if (error.code == "invalid_credentials") R.string.error_current_password else error.userMessageRes())
                    } catch (error: Exception) {
                        ChangePasswordState.Error(error.userMessageRes())
                    }
                }
                ChangePasswordState.Submitting
            }
        }
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 8
    }
}
