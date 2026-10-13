package id.madsaz.app.feature.profile

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.madsaz.app.R
import id.madsaz.app.data.remote.AccountProfile
import id.madsaz.app.data.remote.ProfileApi
import id.madsaz.app.ui.userMessageRes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface ProfileUiState {
    data object Loading : ProfileUiState

    /** Gagal menyimpan ([errorRes]) tidak mengganti seluruh layar; profil yang tampil tetap profil tersimpan. */
    data class Content(val account: AccountProfile, val saving: Boolean = false, @StringRes val errorRes: Int? = null) : ProfileUiState

    data class Error(@StringRes val messageRes: Int) : ProfileUiState
}

class ProfileViewModel(private val api: ProfileApi) : ViewModel() {
    var uiState: ProfileUiState by mutableStateOf(ProfileUiState.Loading)
        private set

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            uiState = ProfileUiState.Loading
            uiState = try {
                ProfileUiState.Content(withContext(Dispatchers.IO) { api.get() })
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                ProfileUiState.Error(error.userMessageRes())
            }
        }
    }

    fun updateName(name: String) {
        val shown = uiState as? ProfileUiState.Content ?: return
        if (name.isBlank() || name.trim().length > MAX_NAME_LENGTH) {
            uiState = shown.copy(errorRes = R.string.error_name_invalid)
            return
        }
        save(shown) { api.updateName(name.trim()) }
    }

    fun uploadAvatar(jpeg: ByteArray) {
        val shown = uiState as? ProfileUiState.Content ?: return
        save(shown) { api.uploadAvatar(jpeg) }
    }

    fun dismissError() {
        val shown = uiState as? ProfileUiState.Content ?: return
        if (shown.errorRes != null) uiState = shown.copy(errorRes = null)
    }

    private fun save(shown: ProfileUiState.Content, request: () -> AccountProfile) {
        uiState = shown.copy(saving = true, errorRes = null)
        viewModelScope.launch {
            uiState = try {
                ProfileUiState.Content(withContext(Dispatchers.IO) { request() })
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                shown.copy(saving = false, errorRes = error.userMessageRes())
            }
        }
    }

    private companion object {
        /** Batas yang sama dengan validasi PUT /profile di BE. */
        const val MAX_NAME_LENGTH = 80
    }
}
