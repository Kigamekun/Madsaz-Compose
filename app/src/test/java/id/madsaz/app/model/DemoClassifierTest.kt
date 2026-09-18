package id.madsaz.app.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoClassifierTest {
    @Test
    fun classificationIsDeterministicAndAlwaysKnown() {
        val ids = (1..100).map { "madsaz_$it" }

        ids.forEach { id ->
            val first = DemoClassifier.classify(id)
            assertEquals(first, DemoClassifier.classify(id))
            assertTrue(first in CryCategory.entries)
        }
    }
}
