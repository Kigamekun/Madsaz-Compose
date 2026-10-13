package id.madsaz.app.feature.home.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import id.madsaz.app.feature.home.HomeScreen
import id.madsaz.app.ui.components.MadsazScaffold
import id.madsaz.app.ui.components.MainDestination
import id.madsaz.app.ui.preview.PreviewData
import id.madsaz.app.ui.theme.MadsazTheme

@Preview(name = "Beranda · kosong", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun HomeEmptyPreview() {
    MadsazTheme {
        MadsazScaffold(
            selected = MainDestination.HOME,
            onHome = {},
            onHistory = {},
            onRecord = {},
        ) { padding ->
            HomeScreen(
                latestRecording = null,
                historyLoading = false,
                historyError = false,
                onRecord = {},
                onHistory = {},
                onProfile = {},
                onOpenRecording = {},
                contentPadding = padding,
            )
        }
    }
}

@Preview(name = "Beranda · ada hasil", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun HomeWithResultPreview() {
    MadsazTheme {
        MadsazScaffold(
            selected = MainDestination.HOME,
            onHome = {},
            onHistory = {},
            onRecord = {},
        ) { padding ->
            HomeScreen(
                latestRecording = PreviewData.latestRecording,
                recentRecordings = PreviewData.recordings,
                historyLoading = false,
                historyError = false,
                onRecord = {},
                onHistory = {},
                onProfile = {},
                onOpenRecording = {},
                contentPadding = padding,
            )
        }
    }
}
