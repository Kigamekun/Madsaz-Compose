package id.madsaz.app.notifications

import java.util.concurrent.TimeUnit
import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import id.madsaz.app.data.remote.NotificationApi

/**
 * Pendaftaran token FCM ke BE. `enabled` adalah pilihan perangkat (izin diberikan dan pengingat menyala);
 * pendaftarannya sendiri per akun. Semua metode memblokir: panggil dari Dispatchers.IO.
 */
class FcmRegistration(context: Context, private val api: NotificationApi) {
    private val appContext = context
    private val preferences = context.getSharedPreferences("madsaz_notifications", Context.MODE_PRIVATE)

    fun enabled(): Boolean = preferences.getBoolean("enabled", false)

    fun permissionPrompted(): Boolean = preferences.getBoolean("permission_prompted", false)

    fun markPermissionPrompted() {
        preferences.edit().putBoolean("permission_prompted", true).apply()
    }

    /** Build tanpa google-services.json tidak punya Firebase; jangan menghabiskan permintaan izin pada build seperti itu. */
    fun configured(): Boolean = FirebaseApp.getApps(appContext).isNotEmpty()

    // Tanpa batas waktu, Play Services yang tidak merespons membuat logout dan pendaftaran menggantung selamanya.
    fun token(): String = Tasks.await(FirebaseMessaging.getInstance().token, TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS)

    /** Hasil dialog izin notifikasi pertama. Bila diberikan, perangkat langsung didaftarkan. */
    fun onPermissionResult(granted: Boolean) {
        // `enabled` ditulis sebelum mendaftar: bila pendaftaran gagal (jaringan), refresh() mencobanya lagi di tiap peluncuran.
        // Dialog izin hanya muncul sekali, jadi tanpa ini perangkat tidak pernah terdaftar.
        preferences.edit().putBoolean("permission_prompted", true).putBoolean("enabled", granted).apply()
        if (granted) {
            check(configured()) { "Firebase belum dikonfigurasi" }
            api.register(token())
        }
    }

    fun enable() {
        check(configured()) { "Firebase belum dikonfigurasi" }
        api.register(token())
        preferences.edit().putBoolean("enabled", true).apply()
    }

    /** Mendaftarkan ulang token untuk akun yang sedang masuk (token berputar, atau akun berganti di perangkat ini). */
    fun refresh() {
        if (enabled() && configured()) api.register(token())
    }

    /** Pengguna mematikan pengingat. */
    fun disable() {
        preferences.edit().putBoolean("enabled", false).apply()
        unregisterDevice()
    }

    /** Dipanggil saat logout: akun ini berhenti menerima push, pilihan perangkat (`enabled`) tetap. */
    fun unregisterDevice() {
        if (!configured()) return
        val token = token()
        try {
            api.unregister(token)
        } finally {
            awaitDeleteToken()
        }
    }

    /** Akun dihapus: token sudah tidak ada di BE, cukup lepaskan token di perangkat. */
    fun clearLocal() {
        if (configured()) awaitDeleteToken()
    }

    private fun awaitDeleteToken() {
        Tasks.await(FirebaseMessaging.getInstance().deleteToken(), TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
    }

    private companion object {
        const val TASK_TIMEOUT_SECONDS = 10L
    }
}
