package id.madsaz.app.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel

/** Menyimpan [ViewModelStore] per kunci di ViewModel activity, sehingga selamat dari perubahan konfigurasi. */
private class ScopedStores : ViewModel() {
    val stores = mutableMapOf<String, ViewModelStore>()

    override fun onCleared() {
        stores.values.forEach(ViewModelStore::clear)
        stores.clear()
    }
}

/**
 * Pemilik ViewModel yang hidup selama [key] ada di komposisi (satu sesi login, satu halaman). Isinya dibuang saat
 * kunci berganti atau keluar dari komposisi, tetapi tidak saat Activity dibuat ulang (ganti bahasa, mode gelap, ukuran jendela).
 */
@Composable
fun rememberScopedViewModelStoreOwner(key: String): ViewModelStoreOwner {
    val scoped: ScopedStores = viewModel { ScopedStores() }
    val activity = LocalActivity.current
    val store = scoped.stores.getOrPut(key) { ViewModelStore() }
    DisposableEffect(key) {
        onDispose { if (activity?.isChangingConfigurations != true) scoped.stores.remove(key)?.clear() }
    }
    return remember(store) { object : ViewModelStoreOwner { override val viewModelStore = store } }
}
