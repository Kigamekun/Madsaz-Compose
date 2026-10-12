package id.madsaz.app.feature.history.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import id.madsaz.app.feature.history.HistoryScreen
import id.madsaz.app.feature.history.HistoryUiState
import id.madsaz.app.ui.components.MadsazScaffold
import id.madsaz.app.ui.components.MainDestination
import id.madsaz.app.ui.preview.PreviewData
import id.madsaz.app.ui.theme.MadsazTheme

@Preview(name = "Riwayat · kosong", widthDp = 412, heightDp = 915, showBackground = true)
@Composable
private fun EmptyHistoryPreview() {
    MadsazTheme {
        MadsazScaffold(
            selected = MainDestination.HISTORY,
            onHome = {},
            onHistory = {},
            onProfile = {},
            onRecord = {},
        ) { padding ->
            HistoryScreen(
                state = HistoryUiState.Content(emptyList()),
                onRetry = {},
                onProfile = {},
                onOpenRecording = {},
                onDelete = {},
                contentPadding = padding,
            )
        }
    }
}

@Preview(name = "Riwayat · terisi", widthDp = 412, heightDp = 915, showBackground = true)
@Composable
private fun FilledHistoryPreview() {
    MadsazTheme {
        MadsazScaffold(
            selected = MainDestination.HISTORY,
            onHome = {},
            onHistory = {},
            onProfile = {},
            onRecord = {},
        ) { padding ->
            HistoryScreen(
                state = HistoryUiState.Content(PreviewData.recordings),
                onRetry = {},
                onProfile = {},
                onOpenRecording = {},
                onDelete = {},
                contentPadding = padding,
            )
        }
    }
}
