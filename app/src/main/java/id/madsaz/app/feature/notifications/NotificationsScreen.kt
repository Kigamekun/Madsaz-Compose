package id.madsaz.app.feature.notifications

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import id.madsaz.app.notifications.FcmRegistration
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.components.tr
import id.madsaz.app.ui.theme.AppBackground
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.Ink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(accessToken: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val registration = remember(context) { FcmRegistration(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var enabled by remember { mutableStateOf(registration.enabled()) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val permissionError = tr("Allow notifications in Android settings.", "Izinkan notifikasi di pengaturan Android.")
    val disableError = tr("Could not turn reminders off.", "Pengingat gagal dimatikan.")
    fun enable() {
        scope.launch {
            busy = true
            error = null
            try {
                withContext(Dispatchers.IO) { registration.enable(accessToken) }
                enabled = true
            } catch (exception: Exception) {
                error = exception.message ?: "Pengingat gagal diaktifkan."
            } finally {
                busy = false
            }
        }
    }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            enable()
        } else {
            error = permissionError
        }
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { Text(tr("Notifications", "Notifikasi"), color = Ink, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, tr("Back", "Kembali"), tint = Ink) } },
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Text(tr("Plan reminders", "Pengingat paket"), color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Switch(
                            checked = enabled,
                            enabled = !busy,
                            onCheckedChange = { checked ->
                                error = null
                                if (checked) {
                                    if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                        permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else enable()
                                } else {
                                    scope.launch {
                                        busy = true
                                        try {
                                            withContext(Dispatchers.IO) { registration.disable(accessToken) }
                                            enabled = false
                                        } catch (exception: Exception) {
                                            error = exception.message ?: disableError
                                        } finally {
                                            enabled = registration.enabled()
                                            busy = false
                                        }
                                    }
                                }
                            },
                        )
                    }
                }
                if (error != null) item { Text(error.orEmpty(), color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}
