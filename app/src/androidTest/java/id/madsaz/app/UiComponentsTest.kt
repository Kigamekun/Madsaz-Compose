package id.madsaz.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import id.madsaz.app.data.remote.AccountProfile
import id.madsaz.app.feature.profile.ProfileScreen
import id.madsaz.app.feature.profile.ProfileUiState
import id.madsaz.app.feature.profile.PrivacyScreen
import id.madsaz.app.feature.profile.PrivacyUiState
import id.madsaz.app.feature.auth.DeleteAccountState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
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

    @Test fun empty_state_invokes_record_action() {
        var calls = 0
        compose.setContent {
            MadsazTheme { RecordingEmptyState("Belum ada rekaman", "Mulai rekaman pertama Anda.", { calls++ }) }
        }
        compose.onNodeWithTag("empty_record").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(1, calls) }
    }

    @Test fun privacy_page_keeps_its_title_visible_while_loading_and_back_is_immediate() {
        var backs = 0
        compose.setContent {
            MadsazTheme { PrivacyScreen(PrivacyUiState.Loading, onRetry = {}, onBack = { backs++ }) }
        }
        compose.onNodeWithText(context.getString(R.string.profile_privacy_title)).assertIsDisplayed()
        compose.onNodeWithContentDescription(context.getString(R.string.common_back)).performClick()
        compose.runOnIdle { assertEquals(1, backs) }
    }

    @Test fun shared_field_preserves_input_error_password_and_disabled_states() {
        var value by mutableStateOf("")
        var enabled by mutableStateOf(true)
        compose.setContent {
            MadsazTheme {
                Column {
                    MadsazTextField(value, { value = it }, "Kata sandi", Modifier.testTag("field"), secret = true, isError = true, enabled = enabled)
                }
            }
        }
        compose.onNodeWithTag("field").performTextInput("test-password")
        compose.onNodeWithTag("field").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Password))
        compose.onNodeWithTag("field").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Error))
        compose.onNodeWithContentDescription(context.getString(R.string.auth_show_password)).performClick()
        compose.onNodeWithTag("field").assertTextContains("test-password")
        compose.runOnIdle { assertEquals("test-password", value); enabled = false }
        compose.onNodeWithTag("field").assertIsNotEnabled()
    }

    @Test fun recorder_switch_start_timer_stop_and_back_use_callbacks() {
        var state: RecordingUiState by mutableStateOf(RecordingUiState.Ready)
        var switches = 0
        var stops = 0
        var backs = 0
        compose.setContent {
            MadsazTheme {
                RecordingScreen(state, false, true, { backs++ }, {}, {}, { switches++ },
                    { state = RecordingUiState.Recording(7) }, { stops++ },
                    cameraPreview = { Box(it) })
            }
        }
        compose.onNodeWithTag("recording_switch").performClick()
        compose.onNodeWithTag("recording_start").performClick()
        compose.onNodeWithTag("recording_switch").assertDoesNotExist()
        compose.onNodeWithTag("recording_timer").assertTextEquals("00:07")
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

    @Test fun profile_edit_remains_usable_with_large_text_and_keyboard() {
        var savedName: String? = null
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 2f)) {
                MadsazTheme {
                    ProfileScreen(
                        state = ProfileUiState.Content(AccountProfile("fixture", "Orang Tua", "parent@example.test", null)),
                        recordingCount = 0, storageBytes = 0, onRefresh = {}, onUpdateName = { savedName = it },
                        onUploadAvatar = {}, onDismissProfileError = {}, onLogout = {}, deleteState = DeleteAccountState.Idle,
                        onDeleteAccount = {}, onDismissDeleteAccount = {}, onPlan = {}, onNotifications = {}, onPrivacy = {}, onLanguage = {},
                        contentPadding = PaddingValues(),
                    )
                }
            }
        }
        compose.onNodeWithTag("profile_edit").performClick()
        compose.onNodeWithTag("profile_name").performScrollTo().performClick().performTextReplacement("Nama Uji")
        compose.onNodeWithTag("profile_save").performScrollTo().assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals("Nama Uji", savedName) }
    }

    @Test fun profile_logout_disables_repeat_taps_while_request_is_running() {
        var logoutCalls = 0
        compose.setContent {
            MadsazTheme {
                ProfileScreen(
                    state = ProfileUiState.Content(AccountProfile("fixture", "Orang Tua", "parent@example.test", null)),
                    recordingCount = 0,
                    storageBytes = 0,
                    onRefresh = {},
                    onUpdateName = {},
                    onUploadAvatar = {},
                    onDismissProfileError = {},
                    onLogout = { logoutCalls++ },
                    deleteState = DeleteAccountState.Idle,
                    onDeleteAccount = {},
                    onDismissDeleteAccount = {},
                    onPlan = {},
                    onNotifications = {},
                    onPrivacy = {},
                    onLanguage = {},
                    contentPadding = PaddingValues(),
                    loggingOut = true,
                )
            }
        }
        compose.onNodeWithTag("profile_logout").assertIsNotEnabled()
        compose.runOnIdle { assertEquals(0, logoutCalls) }
    }

    @Test fun bottom_navigation_exposes_selected_destination_and_actions() {
        var selected by mutableStateOf(MainDestination.HOME)
        var records = 0
        compose.setContent {
            MadsazTheme {
                MadsazScaffold(selected, { selected = MainDestination.HOME }, { selected = MainDestination.HISTORY }, { records++ }) {
                    Box(Modifier.fillMaxSize())
                }
            }
        }
        compose.onNodeWithText(context.getString(R.string.nav_home)).assertIsSelected()
        compose.onNodeWithText(context.getString(R.string.nav_history)).performClick().assertIsSelected()
        compose.onNodeWithContentDescription(context.getString(R.string.nav_record)).performClick()
        compose.runOnIdle { assertEquals(MainDestination.HISTORY, selected); assertEquals(1, records) }
    }
}
