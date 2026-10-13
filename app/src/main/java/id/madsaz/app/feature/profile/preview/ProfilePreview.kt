package id.madsaz.app.feature.profile.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import id.madsaz.app.feature.auth.DeleteAccountState
import id.madsaz.app.feature.profile.ProfileScreen
import id.madsaz.app.feature.profile.ProfileUiState
import id.madsaz.app.data.remote.AccountProfile
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
            onRecord = {},
        ) { padding ->
            ProfileScreen(
                state = ProfileUiState.Content(AccountProfile("preview", "Pandu", "pandu@example.com", null)),
                recordingCount = 3,
                storageBytes = 26_300_000L,
                onRefresh = {},
                onUpdateName = {},
                onUploadAvatar = {},
                onDismissProfileError = {},
                onLogout = {},
                deleteState = DeleteAccountState.Idle,
                onDeleteAccount = {},
                onDismissDeleteAccount = {},
                onPlan = {},
                onNotifications = {},
                onPrivacy = {},
                onLanguage = {},
                contentPadding = padding,
            )
        }
    }
}
