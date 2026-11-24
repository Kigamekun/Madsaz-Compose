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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.unit.dp
import android.view.KeyEvent
import id.madsaz.app.data.remote.PlanStatus
import id.madsaz.app.feature.plan.PlanScreen
import id.madsaz.app.feature.plan.PlanUiState
import id.madsaz.app.feature.home.HomeScreen
import id.madsaz.app.feature.auth.AuthScreen
import id.madsaz.app.feature.auth.AuthUiState
import id.madsaz.app.feature.recording.RecordingScreen
import id.madsaz.app.feature.result.RecordingStatusScreen
import id.madsaz.app.feature.result.ResultScreen
import id.madsaz.app.feature.result.ResultUiState
import id.madsaz.app.model.RecordingStatus
import id.madsaz.app.ui.preview.PreviewData
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

    @Test fun auth_form_keeps_email_but_not_password_when_the_activity_is_recreated() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            MadsazTheme {
                AuthScreen(
                    AuthUiState.SignedOut, startInRegister = false, onSubmit = { _, _, _, _, _, _ -> }, onForgotPassword = {},
                    onTryFree = {}, onOpenTerms = {}, onOpenPrivacy = {}, onLanguage = {},
                )
            }
        }
        compose.onNodeWithTag("auth_email").performTextInput("ibu@example.test")
        compose.onNodeWithTag("auth_password").performTextInput("rahasia-123")

        restoration.emulateSavedInstanceStateRestore()

        compose.onNodeWithText("ibu@example.test").assertExists()
        // Placeholder hanya tampil bila kolom kosong.
        compose.onNodeWithText(context.getString(R.string.auth_placeholder_password)).assertExists()
    }

    @Test fun home_main_card_responds_to_the_accessibility_click_action() {
        var opened = 0
        compose.setContent {
            MadsazTheme {
                HomeScreen(
                    latestRecording = PreviewData.latestRecording, historyLoading = false, historyError = false,
                    onRecord = {}, onHistory = {}, onCryCues = {}, onProfile = {}, onOpenRecording = { opened++ },
                    contentPadding = PaddingValues(0.dp),
                )
            }
        }

        // TalkBack mengaktifkan kontrol lewat aksi semantics, bukan lewat sentuhan.
        compose.onNode(hasText(context.getString(R.string.home_latest_cry)) and hasClickAction()).performSemanticsAction(SemanticsActions.OnClick)

        compose.runOnIdle { assertEquals(1, opened) }
    }

    @Test fun system_back_in_the_plus_offer_returns_to_the_free_plan_instead_of_closing_the_sheet() {
        var closed = 0
        compose.setContent {
            MadsazTheme {
                PlanScreen(
                    PlanUiState(status = PlanStatus.Free(used = 1, limit = 5, remaining = 4), loading = false),
                    onBack = { closed++ }, onRetry = {}, onPullRefresh = {}, onPurchase = { _, _, _ -> }, onDismissAccountInUse = {},
                )
            }
        }
        compose.onNodeWithText(context.getString(R.string.plan_explore_plus)).performClick()
        compose.onNodeWithText(context.getString(R.string.plan_title)).assertDoesNotExist()

        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        compose.waitForIdle()

        compose.runOnIdle { assertEquals(0, closed) }
        compose.onNodeWithText(context.getString(R.string.plan_title)).assertExists()
    }

    @Test fun plan_options_tell_assistive_tech_which_plan_is_selected() {
        compose.setContent {
            MadsazTheme {
                PlanScreen(
                    PlanUiState(status = PlanStatus.Free(used = 1, limit = 5, remaining = 4), loading = false),
                    onBack = {}, onRetry = {}, onPullRefresh = {}, onPurchase = { _, _, _ -> }, onDismissAccountInUse = {},
                )
            }
        }
        compose.onNodeWithText(context.getString(R.string.plan_explore_plus)).performClick()

        compose.onAllNodes(isSelectable()).assertCountEquals(3)
        compose.onAllNodes(isSelected()).assertCountEquals(1)
    }

    @Test fun result_stops_waiting_once_polling_is_exhausted() {
        val processing = PreviewData.latestRecording.copy(status = RecordingStatus.PROCESSING, scores = emptyList())
        var state: ResultUiState by mutableStateOf(ResultUiState.Content(processing))
        compose.setContent { MadsazTheme { ResultScreen(state, { "" }, {}, {}, {}, {}) } }
        compose.onNodeWithText(context.getString(R.string.result_processing_title)).assertExists()

        state = ResultUiState.Content(processing, pollingExhausted = true)

        compose.onNodeWithText(context.getString(R.string.result_processing_title)).assertDoesNotExist()
        compose.onNodeWithText(context.getString(R.string.result_log_pending), substring = true).assertExists()
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
