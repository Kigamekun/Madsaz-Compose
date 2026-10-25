package id.madsaz.app

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import id.madsaz.app.feature.auth.SignedOutApp
import id.madsaz.app.feature.auth.AuthUiState
import id.madsaz.app.feature.auth.AuthViewModel
import id.madsaz.app.feature.feedback.FeedbackScreen
import id.madsaz.app.feature.feedback.FeedbackViewModel
import id.madsaz.app.feature.history.HistoryScreen
import id.madsaz.app.feature.history.HistoryUiState
import id.madsaz.app.feature.history.HistoryViewModel
import id.madsaz.app.feature.home.HomeScreen
import id.madsaz.app.feature.notifications.NotificationsScreen
import id.madsaz.app.feature.notifications.NotificationsViewModel
import id.madsaz.app.feature.plan.PlanScreen
import id.madsaz.app.feature.plan.PlanSummaryViewModel
import id.madsaz.app.feature.plan.PlanViewModel
import id.madsaz.app.feature.plan.UpgradeDialog
import id.madsaz.app.feature.profile.ChangePasswordSheet
import id.madsaz.app.feature.profile.ChangePasswordViewModel
import id.madsaz.app.feature.profile.LanguageScreen
import id.madsaz.app.feature.profile.PrivacyScreen
import id.madsaz.app.feature.profile.PrivacyViewModel
import id.madsaz.app.feature.profile.ProfileScreen
import id.madsaz.app.feature.profile.ProfileUiState
import id.madsaz.app.feature.profile.ProfileViewModel
import id.madsaz.app.feature.recording.RecordingRoute
import id.madsaz.app.feature.recording.RecordingViewModel
import id.madsaz.app.feature.result.ResultScreen
import id.madsaz.app.feature.result.ResultViewModel
import id.madsaz.app.feature.crycues.CryCueDetailScreen
import id.madsaz.app.feature.crycues.CryCuesScreen
import id.madsaz.app.model.CryCue
import id.madsaz.app.data.local.AppLanguage
import id.madsaz.app.notifications.canPostNotifications
import id.madsaz.app.notifications.canRequestNotificationPermission
import id.madsaz.app.data.remote.PlanStatus
import id.madsaz.app.ui.components.MadsazScaffold
import id.madsaz.app.ui.components.MainDestination
import id.madsaz.app.ui.userMessageRes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "MadsazApp"

/** Nilai `destination` pada data push dari BE (src/notifications/send-reminders.js). */
private const val DESTINATION_PLAN = "plan"

private object Routes {
    const val HOME = "home"
    const val HISTORY = "history"
    const val PROFILE = "profile"
    const val CUES = "cues"
    const val CUE_DETAIL = "cue/{code}"
    const val PLAN = "plan"
    const val NOTIFICATIONS = "notifications"
    const val PRIVACY = "privacy"
    const val LANGUAGE = "language"
    const val RECORDING = "recording"
    const val RESULT = "result/{recordingId}?log={log}"
    const val FEEDBACK = "feedback/{recordingId}"

    fun result(recordingId: String, log: Boolean = false) = "result/$recordingId?log=$log"
    fun feedback(recordingId: String) = "feedback/$recordingId"
    fun cue(code: String) = "cue/$code"
}

private enum class AppSheet {
    PLAN,
    NOTIFICATIONS,
    LANGUAGE,
    PASSWORD,
}

internal fun usesDetailTransition(from: String?, to: String?): Boolean {
    val mainRoutes = setOf(Routes.HOME, Routes.HISTORY, Routes.CUES, Routes.PROFILE)
    val routesWithoutPageMotion = setOf(Routes.PLAN, Routes.NOTIFICATIONS, Routes.LANGUAGE, Routes.PRIVACY)
    return from != null && to != null && from !in routesWithoutPageMotion && to !in routesWithoutPageMotion &&
        (from !in mainRoutes || to !in mainRoutes)
}

/** Pemilik ViewModel yang hidup selama satu sesi login; isinya dibuang saat logout atau akun berganti. */
private class SessionViewModelStoreOwner : ViewModelStoreOwner {
    override val viewModelStore = ViewModelStore()
}

@Composable
fun MadsazApp(
    notificationDestination: String? = null,
    onNotificationDestinationHandled: () -> Unit = {},
) {
    val context = LocalContext.current
    val container = context.appContainer
    val authViewModel: AuthViewModel = viewModel {
        AuthViewModel(container.sessionManager, container.authApi, container.fcmRegistration)
    }

    when (val auth = authViewModel.uiState) {
        is AuthUiState.SignedIn -> SignedInApp(
            auth = auth,
            container = container,
            authViewModel = authViewModel,
            notificationDestination = notificationDestination,
            onNotificationDestinationHandled = onNotificationDestinationHandled,
        )
        else -> SignedOutApp(auth, authViewModel, container)
    }
}

@Composable
private fun SignedInApp(
    auth: AuthUiState.SignedIn,
    container: AppContainer,
    authViewModel: AuthViewModel,
    notificationDestination: String?,
    onNotificationDestinationHandled: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // NavController dan ViewModel dibuat ulang untuk setiap sesi login, sehingga data akun sebelumnya tidak terbawa.
    val navController = rememberNavController()
    var appSheet by remember { mutableStateOf<AppSheet?>(null) }
    val sessionOwner = remember(auth.userId) { SessionViewModelStoreOwner() }
    DisposableEffect(sessionOwner) { onDispose { sessionOwner.viewModelStore.clear() } }
    val historyViewModel: HistoryViewModel = viewModel(sessionOwner) { HistoryViewModel(container.recordingApi) }
    val profileViewModel: ProfileViewModel = viewModel(sessionOwner) { ProfileViewModel(container.profileApi) }
    val planSummary: PlanSummaryViewModel = viewModel(sessionOwner) { PlanSummaryViewModel(container.planApi) }
    val passwordViewModel: ChangePasswordViewModel = viewModel(sessionOwner) { ChangePasswordViewModel(container.profileApi, container.sessionManager) }
    var showUpgrade by remember { mutableStateOf(false) }
    val freePlan = planSummary.status as? PlanStatus.Free
    // Daftar rekaman dan sisa kuota dimuat ulang bersamaan setiap layar utama tampil.
    val onMainShown: () -> Unit = { historyViewModel.refresh(); planSummary.refresh() }
    // Kebijakan privasi tersedia dalam id, en, dan ar: sama dengan bahasa aplikasi yang sedang dipakai.
    val policyLanguage = AppLanguage.current(context)
    val accountProfile = (profileViewModel.uiState as? ProfileUiState.Content)?.account
    // Jatah harian habis: tampilkan ajakan upgrade alih-alih membiarkan pengguna merekam lalu ditolak BE setelah mengunggah.
    val openRecorder: () -> Unit = {
        scope.launch {
            try {
                val plan = withContext(Dispatchers.IO) { container.planApi.status() }
                val quotaExhausted = plan is PlanStatus.Free && plan.remaining == 0
                if (quotaExhausted) showUpgrade = true
                else navController.navigate(Routes.RECORDING) { launchSingleTop = true }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Toast.makeText(context, error.userMessageRes(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    val fcm = container.fcmRegistration
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        scope.launch(Dispatchers.IO) { runLoggingFailure("notification permission result") { fcm.onPermissionResult(granted) } }
    }
    LaunchedEffect(auth.userId) {
        // Mendaftarkan perangkat untuk akun ini (token berputar atau akun berganti), lalu meminta izin notifikasi sekali saja.
        withContext(Dispatchers.IO) { runLoggingFailure("FCM refresh") { fcm.refresh() } }
        if (!fcm.configured() || fcm.permissionPrompted()) return@LaunchedEffect
        if (context.canPostNotifications()) {
            withContext(Dispatchers.IO) { runLoggingFailure("FCM enable") { fcm.onPermissionResult(true) } }
        } else {
            fcm.markPermissionPrompted()
            if (canRequestNotificationPermission) {
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Android 7-12 tidak punya dialog izin; notifikasi yang dimatikan di pengaturan dicatat sebagai ditolak.
                withContext(Dispatchers.IO) { runLoggingFailure("notification permission result") { fcm.onPermissionResult(false) } }
            }
        }
    }

    // Pembelian bisa selesai di luar layar Paket (pembayaran tertunda, kode promo): periksa tiap aplikasi kembali aktif.
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(auth.userId) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            runLoggingFailure("billing sync") { container.billing.sync(auth.userId) }
        }
    }

    fun navigateMain(route: String) {
        if (navController.currentDestination?.route == route) return
        if (route == Routes.HOME && navController.popBackStack(Routes.HOME, false)) return
        navController.navigate(route) {
            popUpTo(Routes.HOME) { saveState = route != Routes.HOME }
            launchSingleTop = true
            restoreState = route != Routes.HOME
        }
    }

    val motionDistance = with(LocalDensity.current) { 24.dp.roundToPx() }
    val forwardOffset = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -motionDistance else motionDistance
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = {
            if (usesDetailTransition(initialState.destination.route, targetState.destination.route)) slideInHorizontally(tween(220)) { forwardOffset }
            else EnterTransition.None
        },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = {
            if (usesDetailTransition(initialState.destination.route, targetState.destination.route)) slideOutHorizontally(tween(180)) { forwardOffset }
            else ExitTransition.None
        },
        // Navigation 2.10 has separate gesture transitions; overriding only pop leaves its default fade enabled.
        predictivePopEnterTransition = { EnterTransition.None },
        predictivePopExitTransition = { swipeEdge ->
            if (usesDetailTransition(initialState.destination.route, targetState.destination.route)) {
                slideOutHorizontally(tween(180)) { if (swipeEdge == 0) motionDistance else -motionDistance }
            } else ExitTransition.None
        },
    ) {
        composable(Routes.HOME) {
            val historyState = historyViewModel.uiState
            val content = historyState as? HistoryUiState.Content
            val recent = remember(content) { content?.recordings.orEmpty().take(3) }
            MainDestinationScaffold(MainDestination.HOME, ::navigateMain, openRecorder, onMainShown) { padding ->
                HomeScreen(
                    latestRecording = recent.firstOrNull(),
                    recentRecordings = recent,
                    historyLoading = historyState is HistoryUiState.Loading,
                    historyError = historyState is HistoryUiState.Error,
                    onRecord = openRecorder,
                    onHistory = { navigateMain(Routes.HISTORY) },
                    onCryCues = { navigateMain(Routes.CUES) },
                    onProfile = { navigateMain(Routes.PROFILE) },
                    onOpenRecording = { navController.navigate(Routes.result(it.id, log = true)) },
                    contentPadding = padding,
                    freeQuota = freePlan,
                    onUpgrade = { appSheet = AppSheet.PLAN },
                    avatarUrl = accountProfile?.avatarUrl,
                    accountName = accountProfile?.name,
                )
            }
        }
        composable(Routes.HISTORY) {
            MainDestinationScaffold(MainDestination.HISTORY, ::navigateMain, openRecorder, onMainShown) { padding ->
                HistoryScreen(
                    state = historyViewModel.uiState,
                    onRecord = openRecorder,
                    onRetry = historyViewModel::refresh,
                    onProfile = { navigateMain(Routes.PROFILE) },
                    onOpenRecording = { navController.navigate(Routes.result(it.id, log = true)) },
                    onNoticeShown = historyViewModel::dismissNotice,
                    contentPadding = padding,
                    avatarUrl = accountProfile?.avatarUrl,
                    accountName = accountProfile?.name,
                )
            }
        }
        composable(Routes.CUES) {
            MainDestinationScaffold(MainDestination.CUES, ::navigateMain, openRecorder, onMainShown) { padding ->
                CryCuesScreen(
                    onOpenCue = { navController.navigate(Routes.cue(it.code)) },
                    onProfile = { navigateMain(Routes.PROFILE) },
                    contentPadding = padding,
                    avatarUrl = accountProfile?.avatarUrl,
                    accountName = accountProfile?.name,
                )
            }
        }
        composable(
            route = Routes.CUE_DETAIL,
            arguments = listOf(navArgument("code") { type = NavType.StringType }),
        ) { entry ->
            // Rute hanya dibuat dari CryCue.code, jadi kode di luar lima pola berarti bug navigasi.
            val cue = requireNotNull(CryCue.fromCode(entry.arguments?.getString("code"))) { "Unknown cry cue route" }
            CryCueDetailScreen(cue, onBack = { navController.popBackStack() })
        }
        composable(Routes.PROFILE) {
            // Mulai memuat saat Profil dibuka, lalu pertahankan hasilnya saat halaman privasi ditutup/dibuka kembali.
            val privacyViewModel: PrivacyViewModel = viewModel(
                viewModelStoreOwner = sessionOwner,
                key = "privacy:$policyLanguage",
            ) { PrivacyViewModel { container.policyApi.privacyPolicy(policyLanguage) } }
            val content = historyViewModel.uiState as? HistoryUiState.Content
            MainDestinationScaffold(MainDestination.PROFILE, ::navigateMain, openRecorder, onMainShown) { padding ->
                ProfileScreen(
                    state = profileViewModel.uiState,
                    recordingCount = content?.recordings?.size,
                    storageBytes = content?.totalBytes,
                    onRefresh = profileViewModel::refresh,
                    onUpdateName = profileViewModel::updateName,
                    onUploadAvatar = profileViewModel::uploadAvatar,
                    onDismissProfileError = profileViewModel::dismissError,
                    onLogout = authViewModel::logout,
                    loggingOut = authViewModel.logoutInProgress,
                    deleteState = authViewModel.deleteState,
                    onDeleteAccount = authViewModel::deleteAccount,
                    onDismissDeleteAccount = authViewModel::resetDeleteState,
                    onPlan = { appSheet = AppSheet.PLAN },
                    plusSubtitleRes = if (freePlan != null) R.string.home_upgrade_cta else R.string.profile_plus_subtitle,
                    onChangePassword = { passwordViewModel.reset(); appSheet = AppSheet.PASSWORD },
                    onNotifications = { appSheet = AppSheet.NOTIFICATIONS },
                    onPrivacy = { navController.navigate(Routes.PRIVACY) },
                    onLanguage = { appSheet = AppSheet.LANGUAGE },
                    contentPadding = padding,
                )
            }
        }
        composable(Routes.PRIVACY) {
            val privacyViewModel: PrivacyViewModel = viewModel(
                viewModelStoreOwner = sessionOwner,
                key = "privacy:$policyLanguage",
            ) { PrivacyViewModel { container.policyApi.privacyPolicy(policyLanguage) } }
            PrivacyScreen(privacyViewModel.uiState, onRetry = privacyViewModel::load, onBack = { navController.popBackStack() })
        }
        composable(Routes.RECORDING) {
            val recordingViewModel: RecordingViewModel = viewModel {
                RecordingViewModel(container.recordingStore(auth.userId)) { file, duration -> container.recordingApi.upload(file, duration) }
            }
            RecordingRoute(
                viewModel = recordingViewModel,
                onBack = { navController.popBackStack() },
                onRecordingSaved = { recordingId ->
                    navController.navigate(Routes.result(recordingId)) {
                        popUpTo(Routes.RECORDING) { inclusive = true }
                    }
                },
            )
        }
        composable(
            route = Routes.FEEDBACK,
            arguments = listOf(navArgument("recordingId") { type = NavType.StringType }),
        ) { entry ->
            val recordingId = requireNotNull(entry.arguments?.getString("recordingId"))
            val feedbackViewModel: FeedbackViewModel = viewModel { FeedbackViewModel(recordingId, container.recordingApi) }
            FeedbackScreen(
                state = feedbackViewModel.uiState,
                onSubmit = feedbackViewModel::submit,
                onSubmitted = { navigateMain(Routes.HOME) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.RESULT,
            arguments = listOf(
                navArgument("recordingId") { type = NavType.StringType },
                navArgument("log") { type = NavType.BoolType; defaultValue = false },
            ),
        ) { entry ->
            val recordingId = requireNotNull(entry.arguments?.getString("recordingId"))
            val resultViewModel: ResultViewModel = viewModel { ResultViewModel(recordingId, container.recordingApi) }
            ResultScreen(
                state = resultViewModel.uiState,
                loadVideoUrl = resultViewModel::videoUrl,
                onBack = { navController.popBackStack() },
                onRefresh = resultViewModel::load,
                onFeedback = { navController.navigate(Routes.feedback(it)) },
                onOpenCue = { navController.navigate(Routes.cue(it.code)) },
                startInLog = entry.arguments?.getBoolean("log") == true,
            )
        }
    }

    when (appSheet) {
        AppSheet.PLAN -> {
            val planViewModel: PlanViewModel = viewModel(sessionOwner) {
                PlanViewModel(container.planApi, container.billing, auth.userId)
            }
            PlanScreen(
                state = planViewModel.uiState,
                // Pembelian bisa mengubah paket: perbarui petunjuk kuota saat lembar ditutup.
                onBack = { appSheet = null; planSummary.refresh() },
                onRetry = planViewModel::refresh,
                onPurchase = planViewModel::purchase,
            )
        }
        AppSheet.NOTIFICATIONS -> {
            val notificationsViewModel: NotificationsViewModel = viewModel(sessionOwner) { NotificationsViewModel(fcm) }
            NotificationsScreen(notificationsViewModel, onBack = { appSheet = null })
        }
        AppSheet.LANGUAGE -> LanguageScreen(onBack = { appSheet = null })
        AppSheet.PASSWORD -> ChangePasswordSheet(passwordViewModel.state, onSubmit = passwordViewModel::submit, onDismiss = { appSheet = null })
        null -> Unit
    }

    if (showUpgrade) UpgradeDialog(
        onUpgrade = { showUpgrade = false; appSheet = AppSheet.PLAN },
        onDismiss = { showUpgrade = false },
    )

    LaunchedEffect(notificationDestination) {
        if (notificationDestination == DESTINATION_PLAN) {
            appSheet = AppSheet.PLAN
            onNotificationDestinationHandled()
        }
    }
}

@Composable
private fun MainDestinationScaffold(
    selected: MainDestination,
    navigateMain: (String) -> Unit,
    onRecord: () -> Unit,
    onShown: () -> Unit,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit,
) {
    // Daftar rekaman dimuat ulang setiap layar utama tampil (berpindah tab, kembali dari hasil, atau app dibuka lagi): analisis
    // selesai di BE setelah daftar dimuat, dan akun yang sama bisa menambah rekaman dari perangkat lain.
    LifecycleResumeEffect(Unit) {
        onShown()
        onPauseOrDispose { }
    }
    MadsazScaffold(
        selected = selected,
        onNavigate = { destination ->
            navigateMain(
                when (destination) {
                    MainDestination.HOME -> Routes.HOME
                    MainDestination.HISTORY -> Routes.HISTORY
                    MainDestination.CUES -> Routes.CUES
                    MainDestination.PROFILE -> Routes.PROFILE
                },
            )
        },
        onRecord = onRecord,
        content = content,
    )
}

// Pekerjaan latar yang gagal (mis. jaringan) tidak boleh mengganggu pengguna, tetapi harus tetap terlihat di log.
private inline fun runLoggingFailure(what: String, block: () -> Unit) {
    try {
        block()
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Log.w(TAG, "$what failed", error)
    }
}
