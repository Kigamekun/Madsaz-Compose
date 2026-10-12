package id.madsaz.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import id.madsaz.app.data.remote.BabyProfile
import id.madsaz.app.data.remote.ProfileApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Content(val baby: BabyProfile) : ProfileUiState
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
                ProfileUiState.Content(withContext(Dispatchers.IO) { api.getBabyProfile(accessToken) })
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                ProfileUiState.Error(error.message ?: "Profil bayi gagal dimuat.")
            }
        }
    }

    fun update(name: String, birthDate: String?, weightKg: Double?) {
        if (name.isBlank()) {
            uiState = ProfileUiState.Error("Nama bayi wajib diisi.")
            return
        }
        viewModelScope.launch {
            uiState = try {
                ProfileUiState.Content(withContext(Dispatchers.IO) { api.updateBabyProfile(accessToken, name.trim(), birthDate, weightKg) })
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                ProfileUiState.Error(error.message ?: "Profil bayi gagal disimpan.")
            }
        }
    }
}
