package id.madsaz.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import id.madsaz.app.ui.theme.MadsazTheme

class MainActivity : ComponentActivity() {
    private var notificationDestination by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        notificationDestination = intent.getStringExtra(EXTRA_NOTIFICATION_DESTINATION)
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
        notificationDestination = intent.getStringExtra(EXTRA_NOTIFICATION_DESTINATION)
    }

    companion object {
        const val EXTRA_NOTIFICATION_DESTINATION = "notification_destination"
    }
}
