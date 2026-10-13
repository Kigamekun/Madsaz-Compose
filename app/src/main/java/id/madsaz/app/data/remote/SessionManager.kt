package id.madsaz.app.data.remote

import id.madsaz.app.data.local.AuthSession
import id.madsaz.app.data.local.SessionStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Pemegang sesi tunggal. Access token BE hidup 15 menit dan refresh token hanya berlaku sekali (dirotasi),
 * sehingga refresh harus serial: dua refresh bersamaan akan mencabut sesi sendiri.
 * Metode di sini memblokir; panggil dari Dispatchers.IO.
 */
class SessionManager(private val storage: SessionStorage, private val authApi: AuthApi) : Credentials {
    private val lock = ReentrantLock()
    private val current = MutableStateFlow(storage.read())

    @Volatile
    private var endedByServer = false

    val session: StateFlow<AuthSession?> = current.asStateFlow()

    override fun accessToken(): String =
        current.value?.accessToken ?: throw ApiException(401, "session_ended", "No active session")

    override fun refreshAfterUnauthorized(failedToken: String) = lock.withLock {
        val session = current.value ?: throw ApiException(401, "session_ended", "No active session")
        // Thread lain sudah memperbarui token ini; cukup ulang permintaannya dengan token baru.
        if (session.accessToken != failedToken) return@withLock

        val renewed = try {
            authApi.refresh(session.refreshToken)
        } catch (error: ApiException) {
            // 400/401 berarti refresh token ditolak (kedaluwarsa, sudah dipakai, akun dihapus): tidak bisa dipulihkan.
            if (error.status == 400 || error.status == 401) {
                endedByServer = true
                end()
            }
            throw error
        }
        current.value = renewed
        storage.write(renewed)
    }

    // Tulis dulu, baru umumkan: bila penyimpanan gagal, tidak ada sesi yang terlanjur dianggap masuk.
    fun signIn(session: AuthSession) = lock.withLock {
        storage.write(session)
        endedByServer = false
        current.value = session
    }

    fun signOut() = lock.withLock { end() }

    /** true sekali bila sesi terakhir berakhir karena ditolak server (bukan logout pengguna). */
    fun consumeEndedByServer(): Boolean = endedByServer.also { endedByServer = false }

    private fun end() {
        current.value = null
        storage.clear()
    }
}
