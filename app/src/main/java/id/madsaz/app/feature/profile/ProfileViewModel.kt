package id.madsaz.app.feature.profile

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import id.madsaz.app.R
import id.madsaz.app.data.remote.BabyProfile
import id.madsaz.app.data.remote.ProfileApi
import id.madsaz.app.ui.userMessageRes
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface ProfileUiState {
    data object Loading : ProfileUiState

    /** Gagal menyimpan ([errorRes]) tidak mengganti seluruh layar; profil yang tampil tetap profil tersimpan. */
    data class Content(val baby: BabyProfile, val saving: Boolean = false, @StringRes val errorRes: Int? = null) : ProfileUiState

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
                ProfileUiState.Content(withContext(Dispatchers.IO) { api.getBabyProfile() })
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                ProfileUiState.Error(error.userMessageRes())
            }
        }
    }

    fun update(name: String, birthDate: LocalDate?, weightKg: Double?) {
        val shown = uiState as? ProfileUiState.Content ?: return
        if (name.isBlank()) {
            uiState = shown.copy(errorRes = R.string.error_name_required)
            return
        }
        uiState = shown.copy(saving = true, errorRes = null)
        viewModelScope.launch {
            uiState = try {
                ProfileUiState.Content(withContext(Dispatchers.IO) { api.updateBabyProfile(name.trim(), birthDate, weightKg) })
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                shown.copy(saving = false, errorRes = error.userMessageRes())
            }
        }
    }

    fun dismissError() {
        val shown = uiState as? ProfileUiState.Content ?: return
        if (shown.errorRes != null) uiState = shown.copy(errorRes = null)
    }
}
