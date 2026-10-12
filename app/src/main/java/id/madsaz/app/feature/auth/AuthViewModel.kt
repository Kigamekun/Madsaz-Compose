package id.madsaz.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import id.madsaz.app.data.local.AuthSession
import id.madsaz.app.data.local.AuthStore
import id.madsaz.app.data.remote.AuthApi
import id.madsaz.app.data.remote.AuthApiException
import id.madsaz.app.notifications.FcmRegistration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface AuthUiState {
    data object SignedOut : AuthUiState
    data class SignedIn(val session: AuthSession) : AuthUiState
    data class Error(val message: String) : AuthUiState
    data object Submitting : AuthUiState
}

class AuthViewModel(
    private val store: AuthStore,
    private val fcmRegistration: FcmRegistration,
    private val api: AuthApi = AuthApi(),
) : ViewModel() {
    var uiState: AuthUiState by mutableStateOf(AuthUiState.SignedOut)
        private set

    init {
        val savedSession = store.read()
        if (savedSession == null) {
            uiState = AuthUiState.SignedOut
        } else {
            uiState = AuthUiState.Submitting
            viewModelScope.launch {
                uiState = try {
                    val response = withContext(Dispatchers.IO) { api.refresh(savedSession.refreshToken) }
                    val session = AuthSession(response.userId, response.accessToken, response.refreshToken, response.email, response.name)
                    store.write(session)
                    withContext(Dispatchers.IO) { runCatching { fcmRegistration.refresh(session.accessToken) } }
                    AuthUiState.SignedIn(session)
                } catch (error: CancellationException) {
                    throw error
                } catch (error: AuthApiException) {
                    if (error.statusCode == 401) {
                        store.clear()
                        AuthUiState.SignedOut
                    } else {
                        AuthUiState.Error("Sesi belum dapat diperbarui. Periksa koneksi lalu coba lagi.")
                    }
                } catch (_: Exception) {
                    AuthUiState.Error("Sesi belum dapat diperbarui. Periksa koneksi lalu coba lagi.")
                }
            }
        }
    }

    fun authenticate(register: Boolean, name: String, email: String, password: String) {
        if (email.isBlank() || password.length < 8 || (register && name.isBlank())) {
            uiState = AuthUiState.Error("Lengkapi data dengan benar. Password minimal 8 karakter.")
            return
        }

        viewModelScope.launch {
            uiState = AuthUiState.Submitting
            uiState = try {
                val response = withContext(Dispatchers.IO) {
                    if (register) api.register(name.trim(), email.trim(), password)
                    else api.login(email.trim(), password)
                }
                val session = AuthSession(response.userId, response.accessToken, response.refreshToken, response.email, response.name)
                store.write(session)
                withContext(Dispatchers.IO) { runCatching { fcmRegistration.refresh(session.accessToken) } }
                AuthUiState.SignedIn(session)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                AuthUiState.Error(error.message ?: "Tidak dapat terhubung ke server.")
            }
        }
    }

    fun dismissError() {
        if (uiState is AuthUiState.Error) uiState = AuthUiState.SignedOut
    }

    fun logout() {
        val session = (uiState as? AuthUiState.SignedIn)?.session ?: return
        viewModelScope.launch {
            withContext(Dispatchers.IO) { runCatching { fcmRegistration.disable(session.accessToken) } }
            withContext(Dispatchers.IO) { runCatching { api.logout(session.refreshToken) } }
            store.clear()
            uiState = AuthUiState.SignedOut
        }
    }

}
