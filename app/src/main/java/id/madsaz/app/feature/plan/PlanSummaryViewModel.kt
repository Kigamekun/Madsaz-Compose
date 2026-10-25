package id.madsaz.app.feature.plan

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.madsaz.app.data.remote.PlanApi
import id.madsaz.app.data.remote.PlanStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Paket dan sisa kuota untuk petunjuk upgrade di Beranda/Profil. Hanya informasi tambahan: kegagalan memuat tidak menampilkan galat. */
class PlanSummaryViewModel(private val api: PlanApi) : ViewModel() {
    /** null sampai pertama kali berhasil dimuat; petunjuk disembunyikan selama itu. */
    var status: PlanStatus? by mutableStateOf(null)
        private set

    fun refresh() {
        viewModelScope.launch {
            try {
                status = withContext(Dispatchers.IO) { api.status() }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.w("PlanSummary", "plan status failed", error)
            }
        }
    }
}
