package id.madsaz.app.feature.result.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import id.madsaz.app.R
import id.madsaz.app.feature.result.RecordingStatusScreen
import id.madsaz.app.feature.result.ResultScreen
import id.madsaz.app.feature.result.ResultUiState
import id.madsaz.app.model.RecordingUiState
import id.madsaz.app.ui.preview.PreviewData
import id.madsaz.app.ui.theme.MadsazTheme

@Preview(name = "Hasil · menyimpan", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun SavingPreview() {
    PreviewStatus(RecordingUiState.Saving)
}

@Preview(name = "Hasil · mengunggah", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun UploadingPreview() {
    PreviewStatus(RecordingUiState.Analyzing)
}

@Preview(name = "Hasil · unggahan gagal", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun UploadErrorPreview() {
    PreviewStatus(RecordingUiState.Error(R.string.recording_save_error))
}

@Preview(name = "Hasil · selesai", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun CompletedPreview() {
    PreviewResult(ResultUiState.Content(PreviewData.latestRecording))
}

@Preview(name = "Hasil · memuat", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun LoadingPreview() {
    PreviewResult(ResultUiState.Loading)
}

@Composable
private fun PreviewStatus(state: RecordingUiState) {
    MadsazTheme {
        RecordingStatusScreen(state = state, onRetry = {}, onDiscard = null, onBack = {})
    }
}

@Composable
private fun PreviewResult(state: ResultUiState) {
    MadsazTheme {
        ResultScreen(
            state = state,
            loadVideoUrl = { "" },
            onBack = {},
            onRefresh = {},
            onFeedback = {},
            onOpenCue = {},
        )
    }
}
