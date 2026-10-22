package id.madsaz.app.data.local

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/** Bahasa yang dikirim aplikasi (folder values, values-en, values-ar dan res/resources.properties): kode BCP 47 beserta nama aslinya. */
object AppLanguage {
    val supported = listOf("id" to "Bahasa Indonesia", "en" to "English", "ar" to "العربية")

    /** Bahasa yang sedang dipakai aplikasi: pilihan per-aplikasi bila ada, bila tidak bahasa sistem. */
    fun current(context: Context): String =
        context.resources.configuration.locales[0].toLanguageTag().substringBefore('-')

    /**
     * Per-app language resmi: Android 13+ lewat LocaleManager, Android 7-12 lewat AppCompat (pilihan disimpan oleh
     * AppLocalesMetadataHolderService di manifest). Activity dibuat ulang agar bahasa baru langsung dipakai.
     */
    fun select(languageTag: String) {
        require(supported.any { it.first == languageTag }) { "Unsupported language '$languageTag'" }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))
    }
}
