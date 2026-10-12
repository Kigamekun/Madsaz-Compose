package id.madsaz.app.feature.notifications

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import id.madsaz.app.ui.theme.AppBackground
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk
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
    val configured = registration.configured()

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
        if (granted) enable() else error = "Izinkan notifikasi agar pengingat dapat ditampilkan."
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = Ink, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Ink) } },
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
                item { Text("Pengingat paket", color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
                item { Text("Kami mengingatkan saat paket Plus yang tidak diperpanjang otomatis mendekati akhir masa aktifnya.", color = MutedInk) }
                item {
                    Text(
                        when {
                            !configured -> "Firebase belum dikonfigurasi pada APK ini."
                            enabled -> "Pengingat aktif di perangkat ini."
                            else -> "Pengingat belum aktif di perangkat ini."
                        },
                        color = Ink,
                    )
                }
                if (error != null) item { Text(error.orEmpty(), color = MaterialTheme.colorScheme.error) }
                item {
                    if (busy) CircularProgressIndicator()
                    else if (enabled) OutlinedButton(
                        onClick = {
                            scope.launch {
                                busy = true
                                try {
                                    withContext(Dispatchers.IO) { registration.disable(accessToken) }
                                    enabled = false
                                } catch (exception: Exception) {
                                    error = exception.message ?: "Pengingat gagal dimatikan."
                                } finally {
                                    enabled = registration.enabled()
                                    busy = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Matikan pengingat") }
                    else Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else enable()
                        },
                        enabled = configured,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    ) { Text("Aktifkan pengingat") }
                }
            }
        }
    }
}
