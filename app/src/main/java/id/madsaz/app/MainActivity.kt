package id.madsaz.app

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import android.os.Bundle
import androidx.activity.SystemBarStyle
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import id.madsaz.app.data.local.AppLanguage
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.mutableStateOf
import id.madsaz.app.ui.theme.MadsazTheme

// Android 7.x (API 24-25) tidak bisa menggambar ikon navigasi gelap: androidx.activity memakai darkScrim sebagai warna bilah di sana
// (EdgeToEdgeApi23), jadi harus cukup gelap agar ikon putih terbaca. Nilainya sama dengan DefaultDarkScrim milik androidx.activity.
private val NavigationBarDarkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)

class MainActivity : AppCompatActivity() {
    private var notificationDestination by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        if (AppCompatDelegate.getApplicationLocales().isEmpty) {
            AppLanguage.select("id")
        }
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT), navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, NavigationBarDarkScrim))
        // FCM menampilkan push background tanpa memanggil service, jadi kanal harus sudah ada sebelum pesan tiba.
        // Kanal baru ada di Android 8; NotificationChannelCompat tidak melakukan apa-apa di bawahnya.
        NotificationManagerCompat.from(this).createNotificationChannel(
            NotificationChannelCompat.Builder(getString(R.string.notification_channel_plan_id), NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(getString(R.string.notification_channel_plan_name))
                .build(),
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
