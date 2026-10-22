package id.madsaz.app

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Test

/** values-b+id hanya mendaftarkan locale Indonesia (lihat locale.xml); nilainya tidak boleh menyimpang dari default values/. */
class IndonesianLocaleMarkerTest {
    private fun languageTitle(path: String): String {
        val xml = File(path).readText()
        return Regex("""<string name="language_title">([^<]*)</string>""").find(xml)!!.groupValues[1]
    }

    @Test
    fun indonesian_marker_matches_the_default_string() {
        assertEquals(
            languageTitle("src/main/res/values/strings.xml"),
            languageTitle("src/main/res/values-b+id/locale.xml"),
        )
    }
}
