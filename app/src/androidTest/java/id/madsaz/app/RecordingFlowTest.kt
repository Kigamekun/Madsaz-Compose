package id.madsaz.app

import android.Manifest
import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Camera + upload integration against the disposable local backend, never a user account. */
@RunWith(AndroidJUnit4::class)
class RecordingFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before fun prepare() {
        assumeTrue(BuildConfig.API_BASE_URL.startsWith("http://localhost"))
        grantRuntimePermissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun waitForTag(tag: String) = compose.waitUntil(30_000) {
        compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
    }

    @Test fun record_stop_upload_result_and_main_navigation() {
        waitForTag("auth_toggle")
        compose.onNodeWithTag("auth_toggle").performScrollTo().performClick()
        compose.onNodeWithTag("auth_name").performScrollTo().performTextInput("Orang Tua Uji")
        compose.onNodeWithTag("auth_email").performScrollTo().performTextInput("recording+${System.currentTimeMillis()}@example.test")
        compose.onNodeWithTag("auth_password").performScrollTo().performTextInput("local-ui-test-123")
        compose.onNodeWithTag("auth_confirm").performScrollTo().performTextInput("local-ui-test-123")
        compose.onNodeWithTag("auth_terms").performScrollTo().performClick()
        compose.onNodeWithTag("auth_submit").performScrollTo().performClick()
        waitForTag("home_screen")
        compose.onNodeWithText(context.getString(R.string.nav_history)).performClick()
        compose.onNodeWithTag("empty_record").performScrollTo().performClick()
        waitForTag("recording_start")
        // The switch button only exists on devices with both a front and a back camera.
        if (compose.onAllNodesWithTag("recording_switch").fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithTag("recording_switch").performClick()
        }
        // The record button is enabled only once the preview is streaming (camera bound to the selected lens).
        compose.waitUntil(30_000) {
            compose.onAllNodes(hasTestTag("recording_start") and isEnabled()).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("recording_start").performClick()
        waitForTag("recording_timer")
        compose.waitUntil(15_000) {
            compose.onAllNodesWithTag("recording_timer").fetchSemanticsNodes().any {
                val timer = it.config[SemanticsProperties.Text].first().text
                timer.substringBefore(" /").substringAfter(':').toInt() >= 2
            }
        }
        compose.onNodeWithTag("recording_stop").performClick()
        compose.waitUntil(60_000) {
            compose.onAllNodesWithText(context.getString(R.string.result_title)).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(context.getString(R.string.result_title)).assertIsDisplayed()
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText(context.getString(R.string.nav_home)).performClick()
        waitForTag("home_screen")
        compose.onNodeWithTag("header_profile").performClick()
        waitForTag("profile_screen")
        compose.onNodeWithText(context.getString(R.string.nav_history)).performClick().assertIsSelected()
    }
}
