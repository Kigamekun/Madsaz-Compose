package id.madsaz.app

import android.app.Application
import android.content.Context
import id.madsaz.app.data.billing.PlayBilling
import id.madsaz.app.data.local.AuthStore
import id.madsaz.app.data.local.GuestDevice
import id.madsaz.app.data.local.RecordingStore
import id.madsaz.app.data.remote.ApiClient
import id.madsaz.app.data.remote.AuthApi
import id.madsaz.app.data.remote.GuestApi
import id.madsaz.app.data.remote.GuestResult
import id.madsaz.app.data.remote.NotificationApi
import id.madsaz.app.data.remote.PlanApi
import id.madsaz.app.data.remote.PolicyApi
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
    /** Kebijakan privasi dan S&K bersifat publik: klien tanpa token agar bisa dibuka sebelum masuk. */
    val policyApi = PolicyApi(ApiClient(BuildConfig.API_BASE_URL))
    /** Satu BillingClient untuk seluruh aplikasi; baru dibuat saat pengguna yang masuk membutuhkannya. */
    val billing by lazy { PlayBilling(appContext, planApi) }
    val guestDevice = GuestDevice(appContext)
    val guestApi = GuestApi(ApiClient(BuildConfig.API_BASE_URL))
    /** Hasil Coba Gratis terakhir; hanya di memori karena tidak boleh disimpan. */
    var guestResult: GuestResult? = null
    val fcmRegistration = FcmRegistration(appContext, NotificationApi(api))

    fun recordingStore(userId: String) = RecordingStore(appContext, userId)
}

val Context.appContainer: AppContainer
    get() = (applicationContext as MadsazApplication).container
