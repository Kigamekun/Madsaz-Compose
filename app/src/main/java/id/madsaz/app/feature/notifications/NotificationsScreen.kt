package id.madsaz.app.feature.notifications

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import id.madsaz.app.BuildConfig
import id.madsaz.app.R
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.theme.AppBackground
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
        } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.enable()
        }
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notifications_title), color = Ink, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.common_back), tint = Ink) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground),
            )
        },
    ) { padding ->
        CompactContainer(Modifier.padding(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().toggleable(
                            value = state.enabled,
                            enabled = viewModel.configured && !state.busy,
                            role = Role.Switch,
                            onValueChange = ::setEnabled,
                        ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(stringResource(R.string.notifications_plan_reminders), color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Switch(checked = state.enabled, onCheckedChange = null, enabled = viewModel.configured && !state.busy)
                    }
                }
                if (!viewModel.configured) item { Text(stringResource(R.string.notifications_status_unconfigured), color = MutedInk) }
                if (BuildConfig.DEBUG && viewModel.configured) item {
                    OutlinedButton(
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
                }
                (state.errorRes ?: tokenError)?.let { message ->
                    item { Text(stringResource(message), color = MaterialTheme.colorScheme.error) }
                }
                if (permissionSettingsRequired) item {
                    OutlinedButton(
                        onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(stringResource(R.string.notifications_open_settings)) }
                }
            }
        }
    }
}
