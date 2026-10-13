package id.madsaz.app.data.local

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object AppLanguage {
    private const val KEY = "language"
    private const val PREFERENCES = "madsaz_settings"

    fun selected(context: Context): String = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        .getString(KEY, null) ?: context.resources.configuration.locales[0].language.let { if (it == "id") "id" else "en" }

    fun context(base: Context): Context {
        if (Build.VERSION.SDK_INT >= 33) return base
        val config = Configuration(base.resources.configuration)
        config.setLocale(Locale.forLanguageTag(selected(base)))
        return base.createConfigurationContext(config)
    }

    fun select(activity: Activity, language: String) {
        require(language == "en" || language == "id")
        activity.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit().putString(KEY, language).apply()
        if (Build.VERSION.SDK_INT >= 33) {
            activity.getSystemService(LocaleManager::class.java)
                .applicationLocales = LocaleList.forLanguageTags(language)
        } else {
            activity.recreate()
        }
    }
}
