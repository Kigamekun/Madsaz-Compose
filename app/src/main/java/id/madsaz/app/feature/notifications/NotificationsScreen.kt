package id.madsaz.app.feature.notifications

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.madsaz.app.BuildConfig
import id.madsaz.app.R
import id.madsaz.app.notifications.canPostNotifications
import id.madsaz.app.notifications.canRequestNotificationPermission
import id.madsaz.app.notifications.notificationSettingsIntent
import id.madsaz.app.ui.theme.Border
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk
import id.madsaz.app.ui.userMessageRes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(viewModel: NotificationsViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state = viewModel.uiState
    var permissionSettingsRequired by remember { mutableStateOf(false) }
    var tokenError by remember { mutableStateOf<Int?>(null) }

    // Launcher ini hanya dijalankan di Android 13+ (canRequestNotificationPermission); konstanta izinnya sendiri hanya string, aman di versi lama.
    @SuppressLint("InlinedApi")
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            permissionSettingsRequired = false
            viewModel.enable()
        } else {
            viewModel.showError(R.string.notifications_permission_denied)
            permissionSettingsRequired =
                (context as? Activity)?.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) == false
        }
    }

    fun setEnabled(checked: Boolean) {
        if (!checked) {
            viewModel.disable()
        } else if (!context.canPostNotifications()) {
            if (canRequestNotificationPermission) {
                permission.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.showError(R.string.notifications_permission_denied)
                permissionSettingsRequired = true
            }
        } else {
            viewModel.enable()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onBack,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        dragHandle = null,
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.activity_clock), contentDescription = null, modifier = Modifier.size(56.dp))
                Text(
                    stringResource(R.string.notifications_title),
                    modifier = Modifier.weight(1f).padding(start = 12.dp),
                    color = Ink,
                    fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.Close, stringResource(R.string.common_close), tint = MutedInk)
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Border),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().toggleable(
                        value = state.enabled,
                        enabled = viewModel.configured && !state.busy,
                        role = Role.Switch,
                        onValueChange = ::setEnabled,
                    ).padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.notifications_plan_reminders), Modifier.weight(1f).padding(end = 12.dp), color = Ink, style = MaterialTheme.typography.titleMedium)
                    Switch(checked = state.enabled, onCheckedChange = null, enabled = viewModel.configured && !state.busy)
                }
            }
            if (!viewModel.configured) Text(stringResource(R.string.notifications_status_unconfigured), color = MutedInk)
            if (BuildConfig.DEBUG && viewModel.configured) OutlinedButton(
                onClick = {
                    scope.launch {
                        try {
                            val token = viewModel.token()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("FCM token", token))
                            tokenError = null
                        } catch (error: CancellationException) {
                            throw error
                        } catch (error: Exception) {
                            tokenError = error.userMessageRes()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Salin token uji FCM") }
            (state.errorRes ?: tokenError)?.let { message -> Text(stringResource(message), color = MaterialTheme.colorScheme.error) }
            if (permissionSettingsRequired) OutlinedButton(
                onClick = { context.startActivity(context.notificationSettingsIntent()) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.notifications_open_settings)) }
        }
    }
}
