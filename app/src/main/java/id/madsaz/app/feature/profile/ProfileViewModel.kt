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
import kotlinx.coroutines.Job
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

    /** Hanya untuk indikator tarik-untuk-memuat-ulang. */
    var refreshing by mutableStateOf(false)
        private set

    init { refresh() }

    /** Profil yang sudah tampil dipertahankan selama memuat ulang; gagal memuat ulang ditampilkan sebagai galat sesaat. */
    private var refreshJob: Job? = null

    fun refresh(pulled: Boolean = false) {
        // Respons refresh tidak boleh menimpa simpan yang sedang berjalan (sheet akan tertutup seolah berhasil).
        if ((uiState as? ProfileUiState.Content)?.saving == true) return
        refreshJob?.cancel()
        refreshing = pulled
        refreshJob = viewModelScope.launch {
            val shown = uiState as? ProfileUiState.Content
            if (shown == null) uiState = ProfileUiState.Loading
            uiState = try {
                ProfileUiState.Content(withContext(Dispatchers.IO) { api.get() })
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                shown?.copy(errorRes = error.userMessageRes()) ?: ProfileUiState.Error(error.userMessageRes())
            }
            refreshing = false
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
        // Refresh yang masih berjalan membawa data sebelum simpan: batalkan agar tidak menimpa hasil simpan.
        refreshJob?.cancel()
        refreshing = false
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
