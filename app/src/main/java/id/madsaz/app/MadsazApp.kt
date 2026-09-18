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
import id.madsaz.app.feature.guide.GuideScreen
import id.madsaz.app.feature.crycues.CryCuesScreen
import id.madsaz.app.feature.history.HistoryScreen
import id.madsaz.app.feature.history.HistoryUiState
import id.madsaz.app.feature.history.HistoryViewModel
import id.madsaz.app.feature.home.HomeScreen
import id.madsaz.app.feature.profile.ProfileScreen
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
    const val CRY_CUES = "cry-cues"
    const val PROFILE = "profile"
    const val RECORDING = "recording"
    const val RESULT = "result/{recordingId}"

    fun result(recordingId: String) = "result/$recordingId"
}

@Composable
fun MadsazApp(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val store = remember(context) { RecordingStore(context.applicationContext) }
    val historyViewModel: HistoryViewModel = viewModel { HistoryViewModel(store) }
    val historyState = historyViewModel.uiState

    fun navigateMain(route: String) {
        navController.navigate(route) {
            popUpTo(Routes.HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
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
                    onGuide = { navController.navigate(Routes.CRY_CUES) },
                    onProfile = { navigateMain(Routes.PROFILE) },
                    onHistory = { navigateMain(Routes.HISTORY) },
                    onOpenRecording = { navController.navigate(Routes.result(it.id)) },
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
                    onRetry = historyViewModel::refresh,
                    onRecord = { navController.navigate(Routes.RECORDING) },
                    onOpenRecording = { navController.navigate(Routes.result(it.id)) },
                    onDelete = historyViewModel::delete,
                    contentPadding = padding,
                )
            }
        }
        composable(Routes.CRY_CUES) {
            MadsazScaffold(
                selected = MainDestination.HOME,
                onHome = { navigateMain(Routes.HOME) },
                onHistory = { navigateMain(Routes.HISTORY) },
                onProfile = { navigateMain(Routes.PROFILE) },
                onRecord = { navController.navigate(Routes.RECORDING) },
            ) { padding ->
                CryCuesScreen(
                    onSeeAll = { navigateMain(Routes.HISTORY) },
                    contentPadding = padding,
                )
            }
        }
        composable(Routes.GUIDE) {
            GuideScreen(onBack = { navController.popBackStack() })
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
                    recordingCount = content?.recordings?.size,
                    storageBytes = content?.totalBytes,
                    permissionsGranted = context.hasRecordingPermissions(),
                    onBack = { navController.popBackStack() },
                    modifier = Modifier.padding(padding),
                )
            }
        }
        composable(Routes.RECORDING) {
            val recordingViewModel: RecordingViewModel = viewModel { RecordingViewModel(store) }
            RecordingRoute(
                viewModel = recordingViewModel,
                onBack = { navController.popBackStack() },
                onHistory = {
                    historyViewModel.refresh()
                    navigateMain(Routes.HISTORY)
                },
                onRecordingSaved = historyViewModel::refresh,
            )
        }
        composable(
            route = Routes.RESULT,
            arguments = listOf(navArgument("recordingId") { type = NavType.StringType }),
        ) { entry ->
            val recordingId = requireNotNull(entry.arguments?.getString("recordingId"))
            RecordingResultDestination(
                recordingId = recordingId,
                historyState = historyState,
                onBack = { navController.popBackStack() },
                onRetry = historyViewModel::refresh,
                onRecordAgain = { navController.navigate(Routes.RECORDING) },
                onHistory = { navigateMain(Routes.HISTORY) },
            )
        }
    }
}

@Composable
private fun RecordingResultDestination(
    recordingId: String,
    historyState: HistoryUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onRecordAgain: () -> Unit,
    onHistory: () -> Unit,
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
                    onBack = onBack,
                    onRecordAgain = onRecordAgain,
                    onHistory = onHistory,
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
