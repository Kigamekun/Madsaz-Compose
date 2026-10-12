package id.madsaz.app.notifications

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import id.madsaz.app.data.remote.NotificationApi

class FcmRegistration(private val context: Context, private val api: NotificationApi = NotificationApi()) {
    private val preferences = context.getSharedPreferences("madsaz_notifications", Context.MODE_PRIVATE)

    fun enabled(): Boolean = preferences.getBoolean("enabled", false)

    fun configured(): Boolean = FirebaseApp.getApps(context).isNotEmpty()

    fun enable(accessToken: String) {
        if (!configured()) throw IllegalStateException("Firebase belum dikonfigurasi.")
        val token = Tasks.await(FirebaseMessaging.getInstance().token)
        api.register(accessToken, token)
        preferences.edit().putBoolean("enabled", true).apply()
    }

    fun refresh(accessToken: String) {
        if (!enabled() || !configured()) return
        api.register(accessToken, Tasks.await(FirebaseMessaging.getInstance().token))
    }

    fun disable(accessToken: String) {
        val wasEnabled = enabled()
        preferences.edit().putBoolean("enabled", false).apply()
        if (wasEnabled && configured()) {
            val token = Tasks.await(FirebaseMessaging.getInstance().token)
            try {
                api.unregister(accessToken, token)
            } finally {
                Tasks.await(FirebaseMessaging.getInstance().deleteToken())
            }
        }
    }
}
