package id.madsaz.app

import android.app.Application
import android.content.Context
import id.madsaz.app.data.billing.PlayBilling
import id.madsaz.app.data.local.AuthStore
import id.madsaz.app.data.local.RecordingStore
import id.madsaz.app.data.remote.ApiClient
import id.madsaz.app.data.remote.AuthApi
import id.madsaz.app.data.remote.NotificationApi
import id.madsaz.app.data.remote.PlanApi
import id.madsaz.app.data.remote.ProfileApi
import id.madsaz.app.data.remote.RecordingApi
import id.madsaz.app.data.remote.SessionManager
import id.madsaz.app.notifications.FcmRegistration

class MadsazApplication : Application() {
    val container by lazy { AppContainer(this) }
}

/** Graf dependensi manual: satu SessionManager dan satu ApiClient berotorisasi dipakai semua API. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    val authApi = AuthApi(ApiClient(BuildConfig.API_BASE_URL))
    val sessionManager = SessionManager(AuthStore(appContext), authApi)

    private val api = ApiClient(BuildConfig.API_BASE_URL, sessionManager)
    val recordingApi = RecordingApi(api)
    val planApi = PlanApi(api)
    val profileApi = ProfileApi(api)
    /** Satu BillingClient untuk seluruh aplikasi; baru dibuat saat pengguna yang masuk membutuhkannya. */
    val billing by lazy { PlayBilling(appContext, planApi) }
    val fcmRegistration = FcmRegistration(appContext, NotificationApi(api))

    fun recordingStore(userId: String) = RecordingStore(appContext, userId)
}

val Context.appContainer: AppContainer
    get() = (applicationContext as MadsazApplication).container
