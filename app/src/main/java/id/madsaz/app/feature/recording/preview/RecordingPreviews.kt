package id.madsaz.app.feature.recording.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import id.madsaz.app.R
import id.madsaz.app.feature.recording.RecordingScreen
import id.madsaz.app.model.RecordingUiState
import id.madsaz.app.ui.theme.MadsazTheme

@Preview(name = "Rekam · izin", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun RecordingPermissionPreview() {
    PreviewRecording(RecordingUiState.PermissionRequired)
}

@Preview(name = "Rekam · siap", widthDp = 390, heightDp = 844)
@Composable
private fun RecordingReadyPreview() {
    PreviewRecording(RecordingUiState.Ready)
}

@Preview(name = "Rekam · berjalan", widthDp = 390, heightDp = 844)
@Composable
private fun RecordingActivePreview() {
    PreviewRecording(RecordingUiState.Recording(elapsedSeconds = 12))
}

@Composable
private fun PreviewRecording(state: RecordingUiState) {
    MadsazTheme {
        RecordingScreen(
            state = state,
            permissionPreviouslyRequested = false,
            canSwitchCamera = true,
            onBack = {},
            onRequestPermission = {},
            onOpenSettings = {},
            onSwitchCamera = {},
            onStart = {},
            onStop = {},
            cameraPreview = { previewModifier ->
                Image(
                    painter = painterResource(R.drawable.madsaz_camera_preview),
                    contentDescription = null,
                    modifier = previewModifier,
                    contentScale = ContentScale.FillBounds,
                )
            },
        )
    }
}
