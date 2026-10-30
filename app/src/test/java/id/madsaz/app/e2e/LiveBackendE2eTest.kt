package id.madsaz.app.e2e

import id.madsaz.app.data.local.AuthSession
import id.madsaz.app.data.local.SessionStorage
import id.madsaz.app.data.remote.ApiClient
import id.madsaz.app.data.remote.ApiException
import id.madsaz.app.data.remote.AuthApi
import id.madsaz.app.data.remote.GuestApi
import id.madsaz.app.data.remote.PlanApi
import id.madsaz.app.data.remote.PlanStatus
import id.madsaz.app.data.remote.ProfileApi
import id.madsaz.app.data.remote.RecordingApi
import id.madsaz.app.data.remote.SessionManager
import id.madsaz.app.model.RecordingStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * End-to-end: kode klien yang sama dengan aplikasi (ApiClient, SessionManager, *Api) melawan madsaz-BE sungguhan
 * (Postgres + penyimpanan S3). Dijalankan oleh scripts/e2e.sh, yang menyalakan BE dengan ACCESS_TOKEN_TTL=3s
 * dan MOCK_ANALYSIS_ENABLED=true. Tanpa MADSAZ_E2E_BASE_URL test dilewati.
 */
class LiveBackendE2eTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var baseUrl: String
    private lateinit var authApi: AuthApi
    private lateinit var storage: MemoryStorage
    private lateinit var sessionManager: SessionManager
    private lateinit var recordings: RecordingApi
    private lateinit var plans: PlanApi
    private lateinit var profiles: ProfileApi

    private class MemoryStorage : SessionStorage {
        var stored: AuthSession? = null
        override fun read() = stored
        override fun write(session: AuthSession) { stored = session }
        override fun clear() { stored = null }
    }

    private val password = "password-uji-1"
    private lateinit var email: String

    @Before
    fun setUp() {
        baseUrl = System.getenv("MADSAZ_E2E_BASE_URL").orEmpty()
        assumeTrue("MADSAZ_E2E_BASE_URL tidak diset; jalankan lewat scripts/e2e.sh", baseUrl.isNotEmpty())
        authApi = AuthApi(ApiClient(baseUrl))
        storage = MemoryStorage()
        sessionManager = SessionManager(storage, authApi)
        val api = ApiClient(baseUrl, sessionManager)
        recordings = RecordingApi(api, videoDimensions = { VIDEO_SIZE })
        plans = PlanApi(api)
        profiles = ProfileApi(api)
        email = "android+${UUID.randomUUID()}@example.test"
    }

    private fun registerAndSignIn() {
        sessionManager.signIn(authApi.register("Bayi Uji", email, password, acceptTerms = true))
    }

    private fun video(label: String): File =
        temporaryFolder.newFile("${UUID.randomUUID()}.mp4").also { it.writeText("video-$label-${UUID.randomUUID()}") }

    private fun waitForResult(id: String): id.madsaz.app.model.RecordingItem {
        repeat(30) {
            val recording = recordings.get(id)
            if (recording.status != RecordingStatus.PROCESSING) return recording
            Thread.sleep(500)
        }
        error("Analisis $id tidak selesai")
    }

    @Test
    fun `register, profile update and a fresh plan`() {
        registerAndSignIn()

        assertEquals(PlanStatus.Free(used = 0, limit = 4, remaining = 4), plans.status())
        assertEquals("Bayi Uji", profiles.get().name)

        assertEquals("Pandu", profiles.updateName("Pandu").name)

        // Unggah avatar: upload-url -> PUT ke penyimpanan -> profil menyertakan URL pra-tanda-tangan yang bisa dibaca.
        val photo = ByteArray(2048) { it.toByte() }
        val withAvatar = profiles.uploadAvatar(photo)
        val connection = URL(requireNotNull(withAvatar.avatarUrl)).openConnection() as HttpURLConnection
        try {
            assertEquals(200, connection.responseCode)
            assertTrue(photo.contentEquals(connection.inputStream.readBytes()))
        } finally {
            connection.disconnect()
        }
    }

    @Test
    fun `an expired access token is refreshed transparently and the session keeps working`() {
        registerAndSignIn()
        val before = sessionManager.session.value!!.accessToken

        Thread.sleep(3_500) // ACCESS_TOKEN_TTL=3s
        val status = plans.status() // 401 -> refresh -> retry, tanpa campur tangan pemanggil

        assertTrue(status is PlanStatus.Free)
        assertNotEquals(before, sessionManager.session.value!!.accessToken)
        assertEquals(sessionManager.session.value, storage.stored)
    }

    @Test
    fun `parallel requests after expiry all succeed although refresh tokens are single use`() {
        registerAndSignIn()
        Thread.sleep(3_500)
        val pool = Executors.newFixedThreadPool(6)

        val results = (1..6).map { pool.submit<PlanStatus> { plans.status() } }
        val statuses = results.map { it.get(30, TimeUnit.SECONDS) }
        pool.shutdownNow()

        assertTrue(statuses.all { it is PlanStatus.Free })
        assertNotEquals(null, sessionManager.session.value)
    }

    @Test
    fun `upload, analysis result, quota counted after the result, and the free limit`() {
        registerAndSignIn()

        val first = video("first")
        val id = recordings.upload(first, durationSeconds = 5)
        val result = waitForResult(id)
        assertEquals(RecordingStatus.COMPLETED, result.status)
        assertEquals(VIDEO_SIZE.first, result.videoWidth)
        assertEquals(VIDEO_SIZE.second, result.videoHeight)
        assertEquals(5, result.scores.size)
        assertTrue(result.sizeBytes > 0)
        assertEquals(PlanStatus.Free(used = 1, limit = 4, remaining = 3), plans.status())

        // Mengulang upload dengan id yang sama idempoten: tidak membuat rekaman ganda dan tidak memakai kuota.
        assertEquals(id, recordings.upload(first, durationSeconds = 5))
        assertEquals(1, plans.status().used)

        for (number in 2..4) {
            waitForResult(recordings.upload(video("recording-$number"), durationSeconds = 3))
        }
        assertEquals(PlanStatus.Free(used = 4, limit = 4, remaining = 0), plans.status())
        assertEquals(4, recordings.list().size)

        val blocked = assertThrows(ApiException::class.java) { recordings.upload(video("fifth"), durationSeconds = 3) }
        assertEquals(403, blocked.status)
        assertEquals("free_limit_reached", blocked.code)
    }

    @Test
    fun `feedback and video playback url work for an uploaded recording`() {
        registerAndSignIn()
        val file = video("playback")
        val id = recordings.upload(file, durationSeconds = 4)
        waitForResult(id)

        recordings.sendFeedback(id, rating = 3, improvement = "Accuracy", comment = "Hasilnya membantu")
        // Hanya penilaian yang wajib.
        recordings.sendFeedback(id, rating = 2, improvement = null, comment = "")

        val connection = URL(recordings.presignedVideoUrl(id)).openConnection() as HttpURLConnection
        try {
            assertEquals(200, connection.responseCode)
            assertEquals(file.readText(), connection.inputStream.readBytes().toString(Charsets.UTF_8))
        } finally {
            connection.disconnect()
        }
    }

    @Test
    fun `changing the password returns a new session and the old password stops working`() {
        registerAndSignIn()
        val wrong = assertThrows(ApiException::class.java) { profiles.changePassword("salah-salah", "kata-sandi-baru-1") }
        assertEquals("invalid_credentials", wrong.code)

        sessionManager.signIn(profiles.changePassword(password, "kata-sandi-baru-1"))

        assertEquals("invalid_credentials", assertThrows(ApiException::class.java) { authApi.login(email, password) }.code)
        assertEquals(email, authApi.login(email, "kata-sandi-baru-1").email)
        assertEquals("Bayi Uji", profiles.get().name)
    }

    @Test
    fun `registering without accepting the terms is rejected`() {
        val error = assertThrows(ApiException::class.java) { authApi.register("Bayi Uji", email, password, acceptTerms = false) }
        assertEquals("terms_required", error.code)
    }

    @Test
    fun `guest analysis returns scores without an account and is limited to two per device`() {
        val device = UUID.randomUUID().toString()
        val guest = GuestApi(ApiClient(baseUrl))
        repeat(2) {
            val result = guest.analyze(video("guest-$it"), device)
            assertEquals(5, result.scores.size)
        }
        val blocked = assertThrows(ApiException::class.java) { guest.analyze(video("guest-3"), device) }
        assertEquals(429, blocked.status)
        assertEquals("guest_limit_reached", blocked.code)
    }

    @Test
    fun `logout revokes the refresh token and login works again`() {
        registerAndSignIn()
        val refreshToken = sessionManager.session.value!!.refreshToken

        authApi.logout(refreshToken)
        sessionManager.signOut()
        val rejected = assertThrows(ApiException::class.java) { authApi.refresh(refreshToken) }
        assertEquals("invalid_refresh_token", rejected.code)

        sessionManager.signIn(authApi.login(email, password))
        assertTrue(plans.status() is PlanStatus.Free)
        assertEquals("invalid_credentials", assertThrows(ApiException::class.java) { authApi.login(email, "salah-salah") }.code)
    }

    @Test
    fun `deleting the account removes it, needs the right password, and ends the session`() {
        registerAndSignIn()
        waitForResult(recordings.upload(video("to-delete"), durationSeconds = 2))

        val wrong = assertThrows(ApiException::class.java) { authApi.deleteAccount(email, "salah-salah") }
        assertEquals(401, wrong.status)
        assertTrue("akun tetap ada setelah password salah", plans.status() is PlanStatus.Free)

        authApi.deleteAccount(email, password)

        assertEquals("invalid_credentials", assertThrows(ApiException::class.java) { authApi.login(email, password) }.code)
        // JWT tanpa status tetap sah sampai kedaluwarsa. Setelah itu refresh ditolak (token ikut terhapus) dan sesi berakhir.
        Thread.sleep(3_500) // ACCESS_TOKEN_TTL=3s
        assertThrows(ApiException::class.java) { plans.status() }
        assertNull(sessionManager.session.value)
        assertTrue(sessionManager.consumeEndedByServer())
    }

    @Test
    fun `registering the same email twice and invalid input map to stable codes`() {
        registerAndSignIn()
        assertEquals("email_taken", assertThrows(ApiException::class.java) { authApi.register("Bayi Lain", email, password, acceptTerms = true) }.code)
        assertEquals("invalid_input", assertThrows(ApiException::class.java) { authApi.register("", "bukan-email", "1", acceptTerms = true) }.code)
    }

    private companion object {
        /** Ukuran video palsu untuk file uji: BE menyimpannya dan mengembalikannya di hasil. */
        val VIDEO_SIZE = 1080 to 1920
    }
}
