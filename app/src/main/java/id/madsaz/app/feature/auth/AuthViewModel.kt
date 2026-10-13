package id.madsaz.app.feature.auth

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.madsaz.app.R
import id.madsaz.app.data.remote.AuthApi
import id.madsaz.app.data.remote.SessionManager
import id.madsaz.app.notifications.FcmRegistration
import id.madsaz.app.ui.userMessageRes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface AuthUiState {
    data object SignedOut : AuthUiState
    data object Submitting : AuthUiState
    data class Error(@StringRes val messageRes: Int) : AuthUiState
    data class SignedIn(val userId: String, val email: String) : AuthUiState
}

sealed interface DeleteAccountState {
    data object Idle : DeleteAccountState
    data object Deleting : DeleteAccountState
    data class Error(@StringRes val messageRes: Int) : DeleteAccountState
}

class AuthViewModel(
    private val sessionManager: SessionManager,
    private val authApi: AuthApi,
    private val fcmRegistration: FcmRegistration,
) : ViewModel() {
    var uiState: AuthUiState by mutableStateOf(
        sessionManager.session.value?.let { AuthUiState.SignedIn(it.userId, it.email) } ?: AuthUiState.SignedOut,
    )
        private set

    var deleteState: DeleteAccountState by mutableStateOf(DeleteAccountState.Idle)
        private set

    init {
        // Sumber kebenaran sesi adalah SessionManager: login, logout, hapus akun, dan refresh yang ditolak server semuanya lewat sini.
        viewModelScope.launch {
            sessionManager.session.collect { session ->
                if (session != null) {
                    uiState = AuthUiState.SignedIn(session.userId, session.email)
                } else if (uiState is AuthUiState.SignedIn) {
                    uiState = if (sessionManager.consumeEndedByServer()) AuthUiState.Error(R.string.error_session_expired) else AuthUiState.SignedOut
                }
            }
        }
    }

    fun authenticate(register: Boolean, name: String, email: String, password: String) {
        // BE hanya mewajibkan 8 karakter saat mendaftar; saat masuk cukup tidak kosong.
        if (!register && (email.isBlank() || password.isBlank())) {
            uiState = AuthUiState.Error(R.string.error_login_incomplete)
            return
        }
        if (register && (email.isBlank() || name.isBlank() || password.length < MIN_PASSWORD_LENGTH)) {
            uiState = AuthUiState.Error(R.string.error_invalid_input)
            return
        }
        viewModelScope.launch {
            uiState = AuthUiState.Submitting
            try {
                withContext(Dispatchers.IO) {
                    val session = if (register) authApi.register(name.trim(), email.trim(), password) else authApi.login(email.trim(), password)
                    sessionManager.signIn(session)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                uiState = AuthUiState.Error(error.userMessageRes())
            }
        }
    }

    fun logout() {
        val refreshToken = sessionManager.session.value?.refreshToken ?: return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    fcmRegistration.unregisterDevice()
                    authApi.logout(refreshToken)
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    // Pembersihan di server bersifat best-effort: logout lokal harus selalu berhasil, refresh token kedaluwarsa sendiri.
                } finally {
                    sessionManager.signOut()
                }
            }
        }
    }

    fun deleteAccount(password: String) {
        val email = (uiState as? AuthUiState.SignedIn)?.email ?: return
        viewModelScope.launch {
            deleteState = DeleteAccountState.Deleting
            try {
                withContext(Dispatchers.IO) {
                    authApi.deleteAccount(email, password)
                    try {
                        fcmRegistration.clearLocal()
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Exception) {
                        // Akun sudah terhapus di BE; token FCM di perangkat tidak lagi berguna dan tidak boleh menghalangi keluar.
                    } finally {
                        sessionManager.signOut()
                    }
                }
                deleteState = DeleteAccountState.Idle
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                deleteState = DeleteAccountState.Error(error.userMessageRes())
            }
        }
    }

    fun resetDeleteState() {
        deleteState = DeleteAccountState.Idle
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 8
    }
}
