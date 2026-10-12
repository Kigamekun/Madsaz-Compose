package id.madsaz.app.notifications

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import id.madsaz.app.data.remote.NotificationApi

class FcmRegistration(private val context: Context, private val api: NotificationApi = NotificationApi()) {
    private val preferences = context.getSharedPreferences("madsaz_notifications", Context.MODE_PRIVATE)

    fun enabled(): Boolean = preferences.getBoolean("enabled", false)

    fun permissionPrompted(): Boolean = preferences.getBoolean("permission_prompted", false)

    fun markPermissionPrompted() {
        preferences.edit().putBoolean("permission_prompted", true).apply()
    }

    fun completeFirstLaunchPermissionRequest(granted: Boolean) {
        preferences.edit()
            .putBoolean("permission_prompted", true)
            .putBoolean("enabled", granted)
            .apply()
    }

    fun configured(): Boolean = FirebaseApp.getApps(context).isNotEmpty()

    fun token(): String = Tasks.await(FirebaseMessaging.getInstance().token)

    fun enable(accessToken: String) {
        if (!configured()) throw IllegalStateException("Firebase belum dikonfigurasi.")
        val token = token()
        api.register(accessToken, token)
        preferences.edit().putBoolean("enabled", true).apply()
    }

    fun refresh(accessToken: String) {
        if (!enabled() || !configured()) return
        api.register(accessToken, token())
    }

    fun disable(accessToken: String) {
        val wasEnabled = enabled()
        preferences.edit().putBoolean("enabled", false).apply()
        if (wasEnabled && configured()) {
            val token = token()
            try {
                api.unregister(accessToken, token)
            } finally {
                Tasks.await(FirebaseMessaging.getInstance().deleteToken())
            }
        }
    }
}
