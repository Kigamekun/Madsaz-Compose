package id.madsaz.app.feature.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.madsaz.app.data.remote.AccountProfile
import id.madsaz.app.data.remote.ProfileApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Content(val account: AccountProfile, val error: String? = null) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

class ProfileViewModel(
    private val accessToken: String,
    private val api: ProfileApi = ProfileApi(),
) : ViewModel() {
    var uiState: ProfileUiState by mutableStateOf(ProfileUiState.Loading)
        private set

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            uiState = try {
                ProfileUiState.Content(withContext(Dispatchers.IO) { api.get(accessToken) })
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                ProfileUiState.Error(error.message ?: "Profil gagal dimuat.")
            }
        }
    }

    fun updateName(name: String) {
        val previous = uiState as? ProfileUiState.Content ?: return
        if (name.isBlank() || name.trim().length > 80) {
            uiState = previous.copy(error = "Nama harus berisi 1–80 karakter.")
            return
        }
        viewModelScope.launch {
            uiState = try {
                ProfileUiState.Content(withContext(Dispatchers.IO) { api.updateName(accessToken, name.trim()) })
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                previous.copy(error = error.message ?: "Profil gagal disimpan.")
            }
        }
    }

    fun uploadAvatar(jpeg: ByteArray) {
        val previous = uiState as? ProfileUiState.Content ?: return
        viewModelScope.launch {
            uiState = try {
                ProfileUiState.Content(withContext(Dispatchers.IO) { api.uploadAvatar(accessToken, jpeg) })
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                previous.copy(error = error.message ?: "Foto gagal diunggah.")
            }
        }
    }
}
