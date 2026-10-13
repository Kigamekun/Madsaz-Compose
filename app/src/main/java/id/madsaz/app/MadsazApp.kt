package id.madsaz.app

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import id.madsaz.app.feature.auth.AuthScreen
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
import id.madsaz.app.feature.plan.PlanViewModel
import id.madsaz.app.feature.profile.ProfileScreen
import id.madsaz.app.feature.profile.ProfileViewModel
import id.madsaz.app.feature.recording.RecordingRoute
import id.madsaz.app.feature.recording.RecordingViewModel
import id.madsaz.app.feature.result.ResultScreen
import id.madsaz.app.feature.result.ResultViewModel
import id.madsaz.app.ui.components.MadsazScaffold
import id.madsaz.app.ui.components.MainDestination
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
    const val PLAN = "plan"
    const val NOTIFICATIONS = "notifications"
    const val RECORDING = "recording"
    const val RESULT = "result/{recordingId}?log={log}"
    const val FEEDBACK = "feedback/{recordingId}"

    fun result(recordingId: String, log: Boolean = false) = "result/$recordingId?log=$log"
    fun feedback(recordingId: String) = "feedback/$recordingId"
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
        else -> AuthScreen(
            state = auth,
            onSubmit = authViewModel::authenticate,
            onForgotPassword = {
                Toast.makeText(context, R.string.auth_forgot_unavailable, Toast.LENGTH_SHORT).show()
            },
        )
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
    val sessionOwner = remember(auth.userId) { SessionViewModelStoreOwner() }
    DisposableEffect(sessionOwner) { onDispose { sessionOwner.viewModelStore.clear() } }
    val historyViewModel: HistoryViewModel = viewModel(sessionOwner) { HistoryViewModel(container.recordingApi) }
    val profileViewModel: ProfileViewModel = viewModel(sessionOwner) { ProfileViewModel(container.profileApi) }

    val fcm = container.fcmRegistration
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        scope.launch(Dispatchers.IO) { runLoggingFailure("notification permission result") { fcm.onPermissionResult(granted) } }
    }
    LaunchedEffect(auth.userId) {
        // Mendaftarkan perangkat untuk akun ini (token berputar atau akun berganti), lalu meminta izin notifikasi sekali saja.
        withContext(Dispatchers.IO) { runLoggingFailure("FCM refresh") { fcm.refresh() } }
        if (!fcm.configured() || fcm.permissionPrompted()) return@LaunchedEffect
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            withContext(Dispatchers.IO) { runLoggingFailure("FCM enable") { fcm.onPermissionResult(true) } }
        } else {
            fcm.markPermissionPrompted()
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
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
        if (route == Routes.HOME && navController.popBackStack(Routes.HOME, false)) return
        navController.navigate(route) {
            popUpTo(Routes.HOME) { saveState = route != Routes.HOME }
            launchSingleTop = true
            restoreState = route != Routes.HOME
        }
    }

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            val historyState = historyViewModel.uiState
            val content = historyState as? HistoryUiState.Content
            val recent = remember(content) { content?.recordings.orEmpty().take(3) }
            MainDestinationScaffold(MainDestination.HOME, navController, ::navigateMain) { padding ->
                HomeScreen(
                    latestRecording = recent.firstOrNull(),
                    recentRecordings = recent,
                    historyLoading = historyState is HistoryUiState.Loading,
                    historyError = historyState is HistoryUiState.Error,
                    onRecord = { navController.navigate(Routes.RECORDING) },
                    onHistory = { navigateMain(Routes.HISTORY) },
                    onProfile = { navigateMain(Routes.PROFILE) },
                    onOpenRecording = { navController.navigate(Routes.result(it.id, log = true)) },
                    contentPadding = padding,
                )
            }
        }
        composable(Routes.HISTORY) {
            MainDestinationScaffold(MainDestination.HISTORY, navController, ::navigateMain) { padding ->
                HistoryScreen(
                    state = historyViewModel.uiState,
                    onRetry = historyViewModel::refresh,
                    onProfile = { navigateMain(Routes.PROFILE) },
                    onOpenRecording = { navController.navigate(Routes.result(it.id, log = true)) },
                    onDelete = historyViewModel::delete,
                    onNoticeShown = historyViewModel::dismissNotice,
                    contentPadding = padding,
                )
            }
        }
        composable(Routes.PROFILE) {
            val content = historyViewModel.uiState as? HistoryUiState.Content
            MainDestinationScaffold(MainDestination.PROFILE, navController, ::navigateMain) { padding ->
                ProfileScreen(
                    babyState = profileViewModel.uiState,
                    recordingCount = content?.recordings?.size,
                    storageBytes = content?.totalBytes,
                    lastRecordingAt = content?.recordings?.firstOrNull()?.recordedAtEpochMillis,
                    onRefresh = profileViewModel::refresh,
                    onUpdateBaby = profileViewModel::update,
                    onDismissProfileError = profileViewModel::dismissError,
                    onLogout = authViewModel::logout,
                    deleteState = authViewModel.deleteState,
                    onDeleteAccount = authViewModel::deleteAccount,
                    onDismissDeleteAccount = authViewModel::resetDeleteState,
                    onPlan = { navController.navigate(Routes.PLAN) },
                    onNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                    contentPadding = padding,
                )
            }
        }
        composable(Routes.PLAN) {
            val planViewModel: PlanViewModel = viewModel {
                PlanViewModel(container.planApi, container.billing, auth.userId)
            }
            PlanScreen(
                state = planViewModel.uiState,
                onBack = { navController.popBackStack() },
                onRetry = planViewModel::refresh,
                onPurchase = planViewModel::purchase,
            )
        }
        composable(Routes.NOTIFICATIONS) {
            val notificationsViewModel: NotificationsViewModel = viewModel { NotificationsViewModel(fcm) }
            NotificationsScreen(notificationsViewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.RECORDING) {
            val recordingViewModel: RecordingViewModel = viewModel {
                RecordingViewModel(container.recordingStore(auth.userId), container.recordingApi)
            }
            RecordingRoute(
                viewModel = recordingViewModel,
                onBack = { navController.popBackStack() },
                onRecordingSaved = { recordingId ->
                    historyViewModel.refresh()
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
                startInLog = entry.arguments?.getBoolean("log") == true,
            )
        }
    }

    LaunchedEffect(notificationDestination) {
        if (notificationDestination == DESTINATION_PLAN) {
            navController.navigate(Routes.PLAN) { launchSingleTop = true }
            onNotificationDestinationHandled()
        }
    }
}

@Composable
private fun MainDestinationScaffold(
    selected: MainDestination,
    navController: NavHostController,
    navigateMain: (String) -> Unit,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit,
) {
    MadsazScaffold(
        selected = selected,
        onHome = { navigateMain(Routes.HOME) },
        onHistory = { navigateMain(Routes.HISTORY) },
        onRecord = { navController.navigate(Routes.RECORDING) },
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
