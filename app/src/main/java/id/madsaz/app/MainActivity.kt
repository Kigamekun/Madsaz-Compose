package id.madsaz.app

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import androidx.activity.SystemBarStyle
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.mutableStateOf
import id.madsaz.app.ui.theme.MadsazTheme

class MainActivity : ComponentActivity() {
    private var notificationDestination by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT), navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT))
        // FCM menampilkan push background tanpa memanggil service, jadi kanal harus sudah ada sebelum pesan tiba.
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(
                getString(R.string.notification_channel_plan_id),
                getString(R.string.notification_channel_plan_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )
        notificationDestination = intent.getStringExtra(EXTRA_DESTINATION)
        setContent {
            MadsazTheme {
                MadsazApp(
                    notificationDestination = notificationDestination,
                    onNotificationDestinationHandled = { notificationDestination = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationDestination = intent.getStringExtra(EXTRA_DESTINATION)
    }

    companion object {
        // Sama dengan kunci `data.destination` dari BE: FCM menyalin data push background menjadi extra dengan nama itu.
        const val EXTRA_DESTINATION = "destination"
    }
}
