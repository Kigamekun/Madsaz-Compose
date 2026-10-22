package id.madsaz.app.notifications

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.core.app.NotificationManagerCompat

/**
 * Apakah aplikasi boleh menampilkan notifikasi. Android 13+: izin POST_NOTIFICATIONS; Android 7-12: tidak ada izin
 * runtime, yang menentukan hanya pengaturan notifikasi aplikasi.
 */
fun Context.canPostNotifications(): Boolean = NotificationManagerCompat.from(this).areNotificationsEnabled()

/** Dialog izin runtime POST_NOTIFICATIONS hanya ada di Android 13+; di bawahnya pengguna harus mengaktifkannya di pengaturan. */
val canRequestNotificationPermission: Boolean
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

/** Layar pengaturan notifikasi aplikasi (Android 8+), atau detail aplikasi di Android 7.x yang belum punya layar itu. */
fun Context.notificationSettingsIntent(): Intent =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
    }
