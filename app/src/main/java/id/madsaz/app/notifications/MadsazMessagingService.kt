package id.madsaz.app.notifications

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import id.madsaz.app.MainActivity
import id.madsaz.app.R
import id.madsaz.app.appContainer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MadsazMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        val container = appContainer
        // Tanpa sesi tidak ada akun untuk didaftarkan; sign-in berikutnya mendaftarkan token terbaru (SignedInApp).
        if (container.sessionManager.session.value == null) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                container.fcmRegistration.refresh()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.w(TAG, "FCM token registration failed", error)
            }
        }
    }

    // Hanya dipanggil saat app di foreground; saat background FCM menampilkan notifikasinya sendiri
    // (ikon, warna, dan kanal dari meta-data manifest) dan data `destination` sampai sebagai extra intent.
    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: return
        val body = message.notification?.body.orEmpty()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(MainActivity.EXTRA_DESTINATION, message.data["destination"])
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this, getString(R.string.notification_channel_plan_id))
            .setSmallIcon(R.drawable.phosphor_baby)
            .setColor(getColor(R.color.notification_accent))
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify(message.messageId?.hashCode() ?: 1, notification)
        }
    }

    private companion object {
        const val TAG = "MadsazMessaging"
    }
}
