package id.madsaz.app

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.LocaleList
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import id.madsaz.app.data.remote.AccountProfile
import id.madsaz.app.feature.auth.*
import id.madsaz.app.feature.feedback.*
import id.madsaz.app.feature.history.*
import id.madsaz.app.feature.home.HomeScreen
import id.madsaz.app.feature.profile.*
import id.madsaz.app.feature.recording.RecordingScreen
import id.madsaz.app.model.RecordingUiState
import id.madsaz.app.ui.components.*
import id.madsaz.app.ui.theme.MadsazTheme
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.Locale

/** Exercises resource direction, text wrapping and safe control bounds on a real device. */
@RunWith(AndroidJUnit4::class)
class UiLayoutTest {
    @get:Rule val compose = createComposeRule()

    @Test fun compact_and_large_text_layouts_in_all_supported_languages() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val saveScreenshots = InstrumentationRegistry.getArguments().getString("uiScreenshots") == "true"
        var spec by mutableStateOf(LayoutSpec("id", 1f, 360, "home"))
        compose.setContent {
            val registryOwner = LocalActivityResultRegistryOwner.current
            val backDispatcher = LocalOnBackPressedDispatcherOwner.current
            val configuration = Configuration(LocalConfiguration.current).apply {
                setLocales(LocaleList(Locale.forLanguageTag(spec.language)))
                fontScale = spec.scale
                screenWidthDp = spec.width
            }
            val localized = context.createConfigurationContext(configuration)
            val density = LocalDensity.current.density
            CompositionLocalProvider(
                LocalActivityResultRegistryOwner provides checkNotNull(registryOwner),
                LocalOnBackPressedDispatcherOwner provides checkNotNull(backDispatcher),
                LocalContext provides localized,
                LocalConfiguration provides configuration,
                LocalResources provides localized.resources,
                LocalDensity provides Density(density, spec.scale),
                LocalLayoutDirection provides if (spec.language == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr,
            ) {
                MadsazTheme {
                    Box(Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.TopCenter) {
                        Box(Modifier.width(spec.width.dp).fillMaxHeight().testTag("layout")) {
                            key(spec) { Screen(spec.screen) }
                        }
                    }
                }
            }
        }
        val overflows = mutableListOf<String>()
        for (width in listOf(360, 411)) for (scale in listOf(1f, 1.3f, 2f)) {
            for (language in listOf("id", "en", "ar")) for (screen in listOf("home", "history", "profile", "recording", "auth", "feedback")) {
                val next = LayoutSpec(language, scale, width, screen)
                compose.runOnIdle { spec = next }
                compose.waitForIdle()
                if (screen == "recording") {
                    compose.onNodeWithTag("recording_start").assertIsDisplayed()
                    compose.onNodeWithTag("recording_switch").assertIsDisplayed()
                }
                val texts = compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text), useUnmergedTree = true)
                for (index in texts.fetchSemanticsNodes().indices) {
                    val layouts = mutableListOf<TextLayoutResult>()
                    texts[index].performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
                    for (layout in layouts) {
                        if (layout.multiParagraph.height > layout.size.height + 1f || layout.multiParagraph.didExceedMaxLines) {
                            overflows += "$next: ${layout.layoutInput.text}, size=${layout.size}, textHeight=${layout.multiParagraph.height}"
                        }
                    }
                }
                if (saveScreenshots) {
                    val directory = File(context.cacheDir, "ui-audit").apply { mkdirs() }
                    File(directory, "$screen-$width-$scale-$language.png").outputStream().use {
                        compose.onNodeWithTag("layout").captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                }
            }
        }
        assertFalse(overflows.take(20).joinToString("\n"), overflows.isNotEmpty())
    }

    @Composable private fun Screen(screen: String) {
        when (screen) {
            "home" -> MadsazScaffold(MainDestination.HOME, {}, {}, {}) { padding ->
                HomeScreen(null, historyLoading = false, historyError = false, onRecord = {}, onHistory = {}, onProfile = {}, onOpenRecording = {}, contentPadding = padding)
            }
            "history" -> MadsazScaffold(MainDestination.HISTORY, {}, {}, {}) { padding ->
                HistoryScreen(HistoryUiState.Content(emptyList()), {}, {}, {}, {}, {}, padding)
            }
            "profile" -> MadsazScaffold(MainDestination.PROFILE, {}, {}, {}) { padding ->
                ProfileScreen(ProfileUiState.Content(AccountProfile("fixture", "Orang Tua", "parent@example.test", null)), 0, 0, {}, {}, {}, {}, {}, DeleteAccountState.Idle, {}, {}, {}, {}, {}, {}, padding)
            }
            "recording" -> RecordingScreen(RecordingUiState.Ready, false, true, {}, {}, {}, {}, {}, {}, cameraPreview = { Box(it.background(Color(0xFF1C3A53))) })
            "auth" -> AuthScreen(AuthUiState.SignedOut, { _, _, _, _ -> }, {})
            "feedback" -> FeedbackScreen(FeedbackUiState.Idle, { _, _, _ -> }, {})
        }
    }

    private data class LayoutSpec(val language: String, val scale: Float, val width: Int, val screen: String)
}
