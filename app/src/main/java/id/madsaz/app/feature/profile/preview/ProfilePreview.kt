package id.madsaz.app.feature.profile.preview

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import id.madsaz.app.feature.profile.ProfileScreen
import id.madsaz.app.feature.profile.ProfileUiState
import id.madsaz.app.data.remote.BabyProfile
import id.madsaz.app.ui.components.MadsazScaffold
import id.madsaz.app.ui.components.MainDestination
import id.madsaz.app.ui.theme.MadsazTheme

@Preview(name = "Profil", widthDp = 412, heightDp = 915, showBackground = true)
@Composable
private fun ProfilePreview() {
    MadsazTheme {
        MadsazScaffold(
            selected = MainDestination.PROFILE,
            onHome = {},
            onHistory = {},
            onProfile = {},
            onRecord = {},
        ) { padding ->
            ProfileScreen(
                babyState = ProfileUiState.Content(BabyProfile("preview", "Pandu", "2025-02-01", 6.8)),
                recordingCount = 3,
                storageBytes = 26_300_000L,
                lastRecordingAt = null,
                onRefresh = {},
                onUpdateBaby = { _, _, _ -> },
                onLogout = {},
                onPlan = {},
                onNotifications = {},
                modifier = Modifier.padding(padding),
            )
        }
    }
}
