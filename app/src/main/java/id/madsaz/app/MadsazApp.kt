package id.madsaz.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import android.widget.Toast
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import id.madsaz.app.data.local.RecordingStore
import id.madsaz.app.data.local.AuthStore
import id.madsaz.app.feature.auth.AuthScreen
import id.madsaz.app.feature.auth.AuthUiState
import id.madsaz.app.feature.auth.AuthViewModel
import id.madsaz.app.feature.guide.GuideScreen
import id.madsaz.app.feature.education.EducationListScreen
import id.madsaz.app.feature.education.EducationDetailScreen
import id.madsaz.app.feature.history.HistoryScreen
import id.madsaz.app.feature.history.HistoryUiState
import id.madsaz.app.feature.history.HistoryViewModel
import id.madsaz.app.feature.feedback.FeedbackScreen
import id.madsaz.app.feature.home.HomeScreen
import id.madsaz.app.feature.profile.ProfileScreen
import id.madsaz.app.feature.profile.ProfileViewModel
import id.madsaz.app.feature.plan.PlanScreen
import id.madsaz.app.feature.plan.PlanViewModel
import id.madsaz.app.feature.notifications.NotificationsScreen
import id.madsaz.app.notifications.FcmRegistration
import id.madsaz.app.feature.recording.RecordingRoute
import id.madsaz.app.feature.recording.RecordingViewModel
import id.madsaz.app.feature.recording.hasRecordingPermissions
import id.madsaz.app.feature.result.ResultScreen
import id.madsaz.app.model.RecordingUiState
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.components.MadsazScaffold
import id.madsaz.app.ui.components.MainDestination
import id.madsaz.app.ui.theme.AppBackground
import id.madsaz.app.ui.theme.MutedInk

private object Routes {
    const val HOME = "home"
    const val HISTORY = "history"
    const val GUIDE = "guide"
    const val EDUCATION = "education"
    const val EDUCATION_DETAIL = "education/{articleId}"
    const val PROFILE = "profile"
    const val PLAN = "plan"
    const val NOTIFICATIONS = "notifications"
    const val RECORDING = "recording"
    const val RESULT = "result/{recordingId}?log={log}"
    const val FEEDBACK = "feedback/{recordingId}"

    fun result(recordingId: String, log: Boolean = false) = "result/$recordingId?log=$log"
    fun feedback(recordingId: String) = "feedback/$recordingId"
    fun educationDetail(articleId: String) = "education/$articleId"
}

@Composable
fun MadsazApp(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel {
        AuthViewModel(AuthStore(context.applicationContext), FcmRegistration(context.applicationContext))
    }
    val authState = authViewModel.uiState
    fun navigateMain(route: String) {
        if (route == Routes.HOME && navController.popBackStack(Routes.HOME, false)) return
        navController.navigate(route) {
            popUpTo(Routes.HOME) { saveState = route != Routes.HOME }
            launchSingleTop = true
            restoreState = route != Routes.HOME
        }
    }

    if (authState !is AuthUiState.SignedIn) {
        AuthScreen(
            state = authState,
            onSubmit = authViewModel::authenticate,
            onForgotPassword = {
                Toast.makeText(context, "Fitur lupa password belum tersedia.", Toast.LENGTH_SHORT).show()
            },
        )
        return
    }

    val store = remember(context, authState.session.userId) {
        RecordingStore(context.applicationContext, authState.session.userId)
    }
    val historyViewModel: HistoryViewModel = viewModel(key = authState.session.accessToken) {
        HistoryViewModel(authState.session.accessToken)
    }
    val historyState = historyViewModel.uiState

    val profileViewModel: ProfileViewModel = viewModel {
        ProfileViewModel(authState.session.accessToken)
    }

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            val content = historyState as? HistoryUiState.Content
            MadsazScaffold(
                selected = MainDestination.HOME,
                onHome = { navigateMain(Routes.HOME) },
                onHistory = { navigateMain(Routes.HISTORY) },
                onProfile = { navigateMain(Routes.PROFILE) },
                onRecord = { navController.navigate(Routes.RECORDING) },
            ) { padding ->
                HomeScreen(
                    latestRecording = content?.recordings?.firstOrNull(),
                    recentRecordings = content?.recordings?.take(3).orEmpty(),
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
            MadsazScaffold(
                selected = MainDestination.HISTORY,
                onHome = { navigateMain(Routes.HOME) },
                onHistory = { navigateMain(Routes.HISTORY) },
                onProfile = { navigateMain(Routes.PROFILE) },
                onRecord = { navController.navigate(Routes.RECORDING) },
            ) { padding ->
                HistoryScreen(
                    state = historyState,
                    onRetry = { historyViewModel.refresh() },
                    onProfile = { navigateMain(Routes.PROFILE) },
                    onOpenRecording = { navController.navigate(Routes.result(it.id, log = true)) },
                    onDelete = historyViewModel::delete,
                    contentPadding = padding,
                )
            }
        }
        composable(Routes.GUIDE) {
            GuideScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.EDUCATION) {
            EducationListScreen(
                onBack = { navController.popBackStack() },
                onArticle = { navController.navigate(Routes.educationDetail(it)) },
            )
        }
        composable(
            route = Routes.EDUCATION_DETAIL,
            arguments = listOf(navArgument("articleId") { type = NavType.StringType }),
        ) { entry ->
            EducationDetailScreen(
                articleId = requireNotNull(entry.arguments?.getString("articleId")),
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.PROFILE) {
            val content = historyState as? HistoryUiState.Content
            MadsazScaffold(
                selected = MainDestination.PROFILE,
                onHome = { navigateMain(Routes.HOME) },
                onHistory = { navigateMain(Routes.HISTORY) },
                onProfile = { navigateMain(Routes.PROFILE) },
                onRecord = { navController.navigate(Routes.RECORDING) },
            ) { padding ->
                ProfileScreen(
                    babyState = profileViewModel.uiState,
                    recordingCount = content?.recordings?.size,
                    storageBytes = content?.totalBytes,
                    lastRecordingAt = content?.recordings?.firstOrNull()?.recordedAtEpochMillis,
                    onRefresh = profileViewModel::refresh,
                    onUpdateBaby = profileViewModel::update,
                    onLogout = authViewModel::logout,
                    onPlan = { navController.navigate(Routes.PLAN) },
                    onNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                    modifier = Modifier.padding(padding),
                )
            }
        }
        composable(Routes.PLAN) {
            val planViewModel: PlanViewModel = viewModel(key = authState.session.userId) {
                PlanViewModel(context.applicationContext, authState.session.accessToken, authState.session.userId)
            }
            PlanScreen(
                state = planViewModel.uiState,
                onBack = { navController.popBackStack() },
                onRetry = planViewModel::refresh,
                onPurchase = planViewModel::purchase,
            )
        }
        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(
                accessToken = authState.session.accessToken,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.RECORDING) {
            val recordingViewModel: RecordingViewModel = viewModel(key = authState.session.accessToken) {
                RecordingViewModel(store, authState.session.accessToken)
            }
            RecordingRoute(
                viewModel = recordingViewModel,
                accessToken = authState.session.accessToken,
                onBack = { navController.popBackStack() },
                onFeedback = { navController.navigate(Routes.feedback(it)) },
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
            FeedbackScreen(
                recordingId = requireNotNull(entry.arguments?.getString("recordingId")),
                accessToken = authState.session.accessToken,
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
            RecordingResultDestination(
                recordingId = recordingId,
                startInLog = entry.arguments?.getBoolean("log") == true,
                accessToken = authState.session.accessToken,
                historyState = historyState,
                onBack = { navController.popBackStack() },
                onRetry = { historyViewModel.refresh() },
                onRecordAgain = { navController.navigate(Routes.RECORDING) },
                onRefresh = { historyViewModel.refresh() },
                onFeedback = { navController.navigate(Routes.feedback(it)) },
            )
        }
    }
}

@Composable
private fun RecordingResultDestination(
    recordingId: String,
    startInLog: Boolean,
    accessToken: String,
    historyState: HistoryUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onRecordAgain: () -> Unit,
    onRefresh: () -> Unit,
    onFeedback: (String) -> Unit,
) {
    when (historyState) {
        HistoryUiState.Loading -> SimpleStateScreen(onBack = onBack) {
            CircularProgressIndicator()
        }
        is HistoryUiState.Error -> SimpleStateScreen(onBack = onBack) {
            Text(stringResource(historyState.messageRes), textAlign = TextAlign.Center)
            Button(onClick = onRetry) { Text(stringResource(R.string.common_retry)) }
        }
        is HistoryUiState.Content -> {
            val recording = historyState.recordings.firstOrNull { it.id == recordingId }
            if (recording != null) {
                ResultScreen(
                    state = RecordingUiState.Completed(recording),
                    accessToken = accessToken,
                    onRecordAgain = onRecordAgain,
                    onBack = onBack,
                    onRefresh = onRefresh,
                    onFeedback = onFeedback,
                    startInLog = startInLog,
                )
            } else {
                SimpleStateScreen(onBack = onBack) {
                    Text(
                        stringResource(R.string.history_missing_title),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        stringResource(R.string.history_missing_body),
                        color = MutedInk,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SimpleStateScreen(
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground),
            )
        },
    ) { padding ->
        CompactContainer(Modifier.padding(padding)) {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    content = content,
                )
            }
        }
    }
}
