package id.madsaz.app.data.local

import android.app.LocaleManager
import android.content.Context
import android.os.LocaleList

/** Bahasa yang dikirim aplikasi (folder values, values-en, values-ar dan res/resources.properties): kode BCP 47 beserta nama aslinya. */
object AppLanguage {
    val supported = listOf("id" to "Bahasa Indonesia", "en" to "English", "ar" to "العربية")

    /** Bahasa yang sedang dipakai aplikasi: pilihan per-aplikasi bila ada, bila tidak bahasa sistem. */
    fun current(context: Context): String =
        context.resources.configuration.locales[0].toLanguageTag().substringBefore('-')

    /** Per-app language resmi (Android 13+): sistem membuat ulang Activity dan menyimpan pilihan, sama dengan Pengaturan > Bahasa aplikasi. */
    fun select(context: Context, languageTag: String) {
        require(supported.any { it.first == languageTag }) { "Unsupported language '$languageTag'" }
        context.getSystemService(LocaleManager::class.java).applicationLocales = LocaleList.forLanguageTags(languageTag)
    }
}
