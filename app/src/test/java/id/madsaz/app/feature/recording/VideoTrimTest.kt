package id.madsaz.app.feature.recording

import org.junit.Assert.assertEquals
import org.junit.Test

class VideoTrimTest {
    @Test
    fun draggingEndForwardMovesTheStartToKeepFiveSeconds() {
        assertEquals(7_000L to 12_000L, updateClip(0, 0, 12_000, 15_000))
    }

    @Test
    fun draggingStartBackwardMovesTheEndToKeepFiveSeconds() {
        assertEquals(2_000L to 7_000L, updateClip(7_000, 2_000, 12_000, 15_000))
    }

    @Test
    fun eitherHandleCanShortenTheClip() {
        assertEquals(2_000L to 5_000L, updateClip(0, 2_000, 5_000, 15_000))
        assertEquals(0L to 3_000L, updateClip(0, 0, 3_000, 15_000))
    }

    @Test
    fun aVideoShorterThanTheMinimumKeepsItsWholeDuration() {
        assertEquals(0L to 100L, updateClip(0, 100, 100, 100))
        assertEquals(0L to 100L, updateClip(0, 0, 0, 100))
    }
}
