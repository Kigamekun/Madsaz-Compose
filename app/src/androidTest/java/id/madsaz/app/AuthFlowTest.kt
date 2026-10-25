package id.madsaz.app

import android.Manifest
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Alur UI nyata di perangkat: daftar -> keluar -> masuk -> hapus akun. Hanya berjalan bila app dibangun dengan
 * -PmadsazApiBaseUrl=http://localhost:<port>/api/v1 (scripts/e2e.sh dengan adb reverse), supaya tidak pernah
 * membuat akun di server sungguhan.
 */
@RunWith(AndroidJUnit4::class)
class AuthFlowTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    // Build ini berisi google-services.json, jadi setelah masuk app meminta izin notifikasi; dialog sistem itu akan menutupi UI yang diuji.
    @Before
    fun grantNotificationPermission() = grantRuntimePermissions(Manifest.permission.POST_NOTIFICATIONS)

    private fun AndroidComposeTestRule<*, *>.waitForTag(tag: String, timeoutMs: Long = 20_000) =
        waitUntil(timeoutMs) { onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }

    // scroll=false untuk node di dalam dialog: AlertDialog tidak scrollable, sehingga performScrollTo gagal di sana.
    private fun AndroidComposeTestRule<*, *>.tap(tag: String, scroll: Boolean = true) {
        waitForTag(tag)
        val node = onNodeWithTag(tag)
        if (scroll) node.performScrollTo()
        node.performClick()
    }

    private fun AndroidComposeTestRule<*, *>.type(tag: String, text: String) {
        waitForTag(tag)
        onNodeWithTag(tag).performTextInput(text)
    }

    private fun logIn(email: String, password: String) {
        compose.type("auth_email", email)
        compose.type("auth_password", password)
        compose.tap("auth_submit")
    }

    private fun openProfile() {
        compose.waitForTag("home_screen")
        compose.tap("header_profile")
    }

    @Test
    fun register_logout_login_and_delete_account() {
        assumeTrue(
            "Hanya terhadap BE lokal (build dengan -PmadsazApiBaseUrl=http://localhost:...)",
            BuildConfig.API_BASE_URL.startsWith("http://localhost"),
        )
        val email = "ui+${System.currentTimeMillis()}@example.test"
        var password = "password-uji-1"

        // Daftar
        compose.tap("auth_toggle")
        compose.type("auth_name", "Bayi Uji")
        compose.type("auth_email", email)
        compose.type("auth_password", password)
        compose.type("auth_confirm", password)
        // Tanpa persetujuan S&K, daftar ditolak sebelum menghubungi BE.
        compose.tap("auth_submit")
        compose.waitUntil(15_000) { compose.onAllNodesWithText(context.getString(R.string.error_terms_required)).fetchSemanticsNodes().isNotEmpty() }
        compose.tap("auth_terms")
        compose.tap("auth_submit")
        compose.waitForTag("home_screen")

        // Ubah kata sandi: kata sandi lama yang salah ditolak, yang benar berhasil dan sesi tetap aktif.
        openProfile()
        compose.tap("profile_password")
        compose.type("password_current", "salah-salah")
        compose.type("password_new", "password-uji-2")
        compose.type("password_confirm", "password-uji-2")
        compose.tap("password_save", scroll = false)
        compose.waitUntil(15_000) { compose.onAllNodesWithText(context.getString(R.string.error_current_password)).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("password_current").performTextReplacement(password)
        compose.tap("password_save", scroll = false)
        compose.waitUntil(15_000) { compose.onAllNodesWithTag("password_save").fetchSemanticsNodes().isEmpty() }
        password = "password-uji-2"

        // Keluar (lembar ubah kata sandi sudah tertutup, Profil masih tampil)
        compose.tap("profile_logout")
        compose.waitForTag("auth_email")

        // Masuk lagi dengan akun yang sama
        logIn(email, password)
        compose.waitForTag("home_screen")

        // Hapus akun: kata sandi salah ditolak dan akun tetap ada
        openProfile()
        compose.tap("profile_delete")
        compose.type("delete_password", "salah-salah")
        compose.tap("delete_confirm", scroll = false)
        val wrongPassword = context.getString(R.string.error_invalid_credentials)
        compose.waitUntil(15_000) { compose.onAllNodesWithText(wrongPassword).fetchSemanticsNodes().isNotEmpty() }

        // Kata sandi benar: akun terhapus dan pengguna kembali ke layar masuk
        compose.onNodeWithTag("delete_password").performTextReplacement(password)
        compose.tap("delete_confirm", scroll = false)
        compose.waitForTag("auth_email")

        // Akun sudah tidak ada: masuk dengan kredensial lama ditolak
        logIn(email, password)
        compose.waitUntil(15_000) { compose.onAllNodesWithText(wrongPassword).fetchSemanticsNodes().isNotEmpty() }
    }
}
