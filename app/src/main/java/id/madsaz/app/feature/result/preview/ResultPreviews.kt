package id.madsaz.app.feature.result.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import id.madsaz.app.R
import id.madsaz.app.feature.result.ResultScreen
import id.madsaz.app.model.RecordingUiState
import id.madsaz.app.ui.preview.PreviewData
import id.madsaz.app.ui.theme.MadsazTheme

@Preview(name = "Hasil · menyimpan", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun SavingPreview() {
    PreviewResult(RecordingUiState.Saving)
}

@Preview(name = "Hasil · menganalisis", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun AnalyzingPreview() {
    PreviewResult(RecordingUiState.Analyzing)
}

@Preview(name = "Hasil · selesai", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun CompletedPreview() {
    PreviewResult(RecordingUiState.Completed(PreviewData.latestRecording))
}

@Preview(name = "Hasil · gagal", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun ErrorPreview() {
    PreviewResult(RecordingUiState.Error(R.string.recording_save_error))
}

@Composable
private fun PreviewResult(state: RecordingUiState) {
    MadsazTheme {
        ResultScreen(
            state = state,
            accessToken = "preview",
            onRecordAgain = {},
            onBack = {},
            onFeedback = {},
        )
    }
}
