package id.madsaz.app

import id.madsaz.app.data.local.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {
    @Test
    fun supported_languages_have_their_flags() {
        assertEquals(listOf("🇮🇩", "🇬🇧", "🇸🇦"), AppLanguage.supported.map { AppLanguage.flag(it.first) })
    }

    @Test
    fun unrecognized_language_uses_indonesian_flag() {
        assertEquals("🇮🇩", AppLanguage.flag("fr"))
    }
}
