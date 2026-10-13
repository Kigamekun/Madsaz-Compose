package id.madsaz.app.feature.notifications

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.madsaz.app.notifications.FcmRegistration
import id.madsaz.app.ui.userMessageRes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class NotificationsUiState(
    val enabled: Boolean,
    val busy: Boolean = false,
    @StringRes val errorRes: Int? = null,
)

class NotificationsViewModel(private val fcm: FcmRegistration) : ViewModel() {
    var uiState by mutableStateOf(NotificationsUiState(enabled = fcm.enabled()))
        private set

    val configured: Boolean = fcm.configured()

    fun enable() = change(enabled = true) { fcm.enable() }

    fun disable() = change(enabled = false) { fcm.disable() }

    fun showError(@StringRes messageRes: Int) {
        uiState = uiState.copy(errorRes = messageRes)
    }

    suspend fun token(): String = withContext(Dispatchers.IO) { fcm.token() }

    private fun change(enabled: Boolean, action: () -> Unit) {
        viewModelScope.launch {
            uiState = uiState.copy(busy = true, errorRes = null)
            try {
                withContext(Dispatchers.IO) { action() }
                uiState = uiState.copy(enabled = enabled, busy = false)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                uiState = uiState.copy(enabled = fcm.enabled(), busy = false, errorRes = error.userMessageRes())
            }
        }
    }
}
