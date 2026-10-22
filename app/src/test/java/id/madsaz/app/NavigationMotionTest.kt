package id.madsaz.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationMotionTest {
    @Test fun main_destinations_and_initial_entry_do_not_animate() {
        for (from in listOf("home", "history", "cues", "profile")) {
            for (to in listOf("home", "history", "cues", "profile")) assertFalse(usesDetailTransition(from, to))
        }
        assertFalse(usesDetailTransition(null, "home"))
    }

    @Test fun detail_destinations_animate_in_both_directions() {
        for (detail in listOf("recording", "result/{recordingId}?log={log}", "cue/{code}")) {
            assertTrue(usesDetailTransition("home", detail))
            assertTrue(usesDetailTransition(detail, "home"))
        }
        assertFalse(usesDetailTransition("profile", "privacy"))
        assertFalse(usesDetailTransition("privacy", "profile"))
    }

    @Test fun sheet_destinations_do_not_use_page_motion() {
        for (sheet in listOf("plan", "notifications", "language")) {
            assertFalse(usesDetailTransition("profile", sheet))
            assertFalse(usesDetailTransition(sheet, "profile"))
        }
    }
}
