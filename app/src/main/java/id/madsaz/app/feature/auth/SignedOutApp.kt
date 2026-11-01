package id.madsaz.app.feature.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import id.madsaz.app.AppContainer
import id.madsaz.app.R
import id.madsaz.app.appContainer
import id.madsaz.app.data.local.AppLanguage
import id.madsaz.app.data.local.GUEST_STORE_ID
import id.madsaz.app.data.remote.PrivacyPolicy
import id.madsaz.app.feature.profile.LanguageScreen
import id.madsaz.app.feature.profile.PrivacyScreen
import id.madsaz.app.feature.profile.PrivacyViewModel
import id.madsaz.app.feature.recording.RecordingRoute
import id.madsaz.app.feature.recording.RecordingViewModel
import id.madsaz.app.feature.result.PatternResult
import id.madsaz.app.ui.theme.BrandBlueDark
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Halaman sebelum masuk. Masuk, lupa kata sandi, S&K, dan privasi ditumpuk di atas [AuthScreen] yang tetap tersusun
 * agar isian formulir tidak hilang; rekam dan hasil Coba Gratis menggantikan layar penuh.
 */
private enum class Page { AUTH, FORGOT, TERMS, PRIVACY, GUEST_RECORD, GUEST_RESULT }

private const val GUEST_RESULT_ID = "guest"

@Composable
fun SignedOutApp(auth: AuthUiState, authViewModel: AuthViewModel, container: AppContainer) {
    val context = LocalContext.current
    var page by rememberSaveable { mutableStateOf(Page.AUTH) }
    var registerRequested by rememberSaveable { mutableStateOf(false) }
    var showLanguage by rememberSaveable { mutableStateOf(false) }
    var showGuestLimit by rememberSaveable { mutableStateOf(false) }
    var guestDailyLimit by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(container) {
        try {
            guestDailyLimit = withContext(Dispatchers.IO) { container.guestApi.dailyLimit() }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            android.util.Log.w("GuestQuota", "Could not load guest daily limit", error)
        }
    }
    // Bahasa dibaca setiap komposisi: setelah pengguna berganti bahasa Activity dibuat ulang dan dokumen dimuat ulang dalam bahasa baru.
    val language = AppLanguage.current(context)

    when (page) {
        Page.GUEST_RECORD -> GuestRecording(container, onBack = { page = Page.AUTH }, onResult = { page = Page.GUEST_RESULT })
        Page.GUEST_RESULT -> {
            val result = container.guestResult
            if (result == null) page = Page.AUTH
            else {
                BackHandler { container.guestResult = null; page = Page.AUTH }
                PatternResult(
                    scores = result.scores,
                    onDone = { container.guestResult = null; page = Page.AUTH },
                    onOpenCue = {},
                    modifier = Modifier.background(Color.White),
                    actions = {
                        Button(
                            onClick = { container.guestResult = null; page = Page.AUTH },
                            modifier = Modifier.weight(1f).heightIn(min = 52.dp).testTag("guest_done"),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlueDark),
                        ) { Text(stringResource(R.string.result_done)) }
                    },
                )
            }
        }
        else -> Box(Modifier.fillMaxSize()) {
            // Saat dokumen atau lupa kata sandi menutupinya, layar masuk tetap tersusun (isian tidak hilang) tetapi tidak boleh bisa difokus pembaca layar.
            Box(if (page == Page.AUTH) Modifier else Modifier.clearAndSetSemantics {}) { AuthScreen(
                state = auth,
                startInRegister = registerRequested,
                guestDailyLimit = guestDailyLimit,
                onSubmit = authViewModel::authenticate,
                onForgotPassword = { page = Page.FORGOT },
                onTryFree = {
                    val limit = guestDailyLimit
                    if (limit != null && container.guestDevice.analysesToday() >= limit) showGuestLimit = true else page = Page.GUEST_RECORD
                },
                onOpenTerms = { page = Page.TERMS },
                onOpenPrivacy = { page = Page.PRIVACY },
                onLanguage = { showLanguage = true },
            ) }
            when (page) {
                Page.FORGOT -> {
                    BackHandler { page = Page.AUTH }
                    // Dibuang saat halaman ditutup agar membukanya lagi menampilkan formulir, bukan "Cek email" dari permintaan lama.
                    val forgot: ForgotPasswordViewModel = viewModel(rememberPageViewModelStoreOwner()) { ForgotPasswordViewModel(container.authApi, language) }
                    ForgotPasswordScreen(forgot.state, onSend = forgot::send, onBack = { page = Page.AUTH })
                }
                Page.TERMS -> PolicyPage("terms:$language", R.string.auth_terms_link, onBack = { page = Page.AUTH }) { container.policyApi.terms(language) }
                Page.PRIVACY -> PolicyPage("privacy:$language", R.string.profile_privacy_title, onBack = { page = Page.AUTH }) { container.policyApi.privacyPolicy(language) }
                else -> Unit
            }
        }
    }

    if (showLanguage) LanguageScreen(onBack = { showLanguage = false })
    if (showGuestLimit) AlertDialog(
        onDismissRequest = { showGuestLimit = false },
        title = { Text(stringResource(R.string.guest_limit_title)) },
        text = { Text(stringResource(R.string.guest_limit_body)) },
        confirmButton = {
            TextButton(onClick = { showGuestLimit = false; registerRequested = true }, modifier = Modifier.testTag("guest_limit_register")) {
                Text(stringResource(R.string.guest_limit_register), color = BrandBlueDark)
            }
        },
        dismissButton = {
            TextButton(onClick = { showGuestLimit = false; registerRequested = false }) { Text(stringResource(R.string.guest_limit_login)) }
        },
    )
}

@Composable
private fun PolicyPage(key: String, titleRes: Int, onBack: () -> Unit, load: () -> PrivacyPolicy) {
    BackHandler(onBack = onBack)
    val viewModel: PrivacyViewModel = viewModel(key = key) { PrivacyViewModel(load) }
    Box(Modifier.fillMaxSize().background(Color.White)) {
        PrivacyScreen(viewModel.uiState, titleRes = titleRes, onRetry = viewModel::load, onBack = onBack)
    }
}

/** Rekam 5 detik lalu kirim ke BE untuk dianalisis sekali; ViewModel dibuang saat keluar agar percobaan berikutnya mulai bersih. */
@Composable
private fun GuestRecording(container: AppContainer, onBack: () -> Unit, onResult: () -> Unit) {
    val viewModel: RecordingViewModel = viewModel(rememberPageViewModelStoreOwner()) {
        RecordingViewModel(container.recordingStore(GUEST_STORE_ID)) { file, _ ->
            container.guestResult = container.guestApi.analyze(file, container.guestDevice.id())
            container.guestDevice.recordAnalysis()
            GUEST_RESULT_ID
        }
    }
    RecordingRoute(viewModel = viewModel, onBack = onBack, onRecordingSaved = { onResult() })
}

/** Pemilik ViewModel yang hidup selama halaman yang memakainya ada di komposisi; isinya dibuang saat halaman ditutup. */
@Composable
private fun rememberPageViewModelStoreOwner(): ViewModelStoreOwner {
    val owner = remember { object : ViewModelStoreOwner { override val viewModelStore = ViewModelStore() } }
    DisposableEffect(owner) { onDispose { owner.viewModelStore.clear() } }
    return owner
}
