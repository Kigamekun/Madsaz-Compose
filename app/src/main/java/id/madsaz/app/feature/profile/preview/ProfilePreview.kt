package id.madsaz.app.feature.profile.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import id.madsaz.app.feature.auth.DeleteAccountState
import id.madsaz.app.feature.profile.ProfileScreen
import id.madsaz.app.feature.profile.ProfileUiState
import id.madsaz.app.data.remote.BabyProfile
import id.madsaz.app.ui.components.MadsazScaffold
import id.madsaz.app.ui.components.MainDestination
import id.madsaz.app.ui.theme.MadsazTheme
import java.time.LocalDate

@Preview(name = "Profil", widthDp = 412, heightDp = 915, showBackground = true)
@Composable
private fun ProfilePreview() {
    MadsazTheme {
        MadsazScaffold(
            selected = MainDestination.PROFILE,
            onHome = {},
            onHistory = {},
            onRecord = {},
        ) { padding ->
            ProfileScreen(
                babyState = ProfileUiState.Content(BabyProfile("Pandu", LocalDate.of(2025, 2, 1), 6.8)),
                recordingCount = 3,
                storageBytes = 26_300_000L,
                lastRecordingAt = null,
                onRefresh = {},
                onUpdateBaby = { _, _, _ -> },
                onDismissProfileError = {},
                onLogout = {},
                deleteState = DeleteAccountState.Idle,
                onDeleteAccount = {},
                onDismissDeleteAccount = {},
                onPlan = {},
                onNotifications = {},
                contentPadding = padding,
            )
        }
    }
}
