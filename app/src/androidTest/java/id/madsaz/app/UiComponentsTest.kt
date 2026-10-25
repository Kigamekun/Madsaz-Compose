package id.madsaz.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import id.madsaz.app.feature.recording.RecordingScreen
import id.madsaz.app.feature.result.RecordingStatusScreen
import id.madsaz.app.model.RecordingUiState
import id.madsaz.app.ui.components.*
import id.madsaz.app.ui.theme.MadsazTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UiComponentsTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun recorder_switch_start_timer_stop_and_back_use_callbacks() {
        var state: RecordingUiState by mutableStateOf(RecordingUiState.Ready)
        var switches = 0
        var stops = 0
        var backs = 0
        compose.setContent {
            MadsazTheme {
                RecordingScreen(state, false, true, { backs++ }, {}, {}, { switches++ },
                    { state = RecordingUiState.Recording(3) }, { stops++ },
                    cameraPreview = { Box(it) })
            }
        }
        compose.onNodeWithTag("recording_switch").performClick()
        compose.onNodeWithTag("recording_start").performClick()
        compose.onNodeWithTag("recording_switch").assertDoesNotExist()
        compose.onNodeWithTag("recording_timer").assertTextEquals(context.getString(R.string.recording_timer, "00:03", "00:05"))
        compose.onNodeWithTag("recording_stop").assertIsDisplayed().performClick()
        compose.onNodeWithTag("recording_back").performClick()
        compose.runOnIdle { assertEquals(1, switches); assertEquals(1, stops); assertEquals(1, backs) }
    }

    @Test fun upload_failure_can_retry_without_discarding_recording() {
        var retries = 0
        var discarded = 0
        compose.setContent {
            MadsazTheme {
                RecordingStatusScreen(RecordingUiState.Error(R.string.recording_upload_error), { retries++ }, { discarded++ }, {})
            }
        }
        compose.onNodeWithText(context.getString(R.string.common_retry)).performClick()
        compose.runOnIdle { assertEquals(1, retries); assertEquals(0, discarded) }
    }

    @Test fun bottom_navigation_exposes_selected_destination_and_actions() {
        var selected by mutableStateOf(MainDestination.HOME)
        var records = 0
        compose.setContent {
            MadsazTheme {
                MadsazScaffold(selected, { selected = it }, { records++ }) {
                    Box(Modifier.fillMaxSize())
                }
            }
        }
        // Lima slot terlihat berlabel: empat tujuan plus aksi Rekam.
        compose.onNodeWithText(context.getString(R.string.nav_home)).assertIsSelected()
        compose.onNodeWithText(context.getString(R.string.nav_history)).performClick().assertIsSelected()
        compose.onNodeWithText(context.getString(R.string.nav_cues)).performClick().assertIsSelected()
        compose.onNodeWithText(context.getString(R.string.nav_profile)).performClick().assertIsSelected()
        compose.onNodeWithText(context.getString(R.string.nav_record_label)).performClick()
        compose.runOnIdle { assertEquals(MainDestination.PROFILE, selected); assertEquals(1, records) }
    }
}
