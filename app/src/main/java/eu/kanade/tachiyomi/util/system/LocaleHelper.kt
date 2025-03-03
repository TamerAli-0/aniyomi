package eu.kanade.tachiyomi.util.system

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.core.os.LocaleListCompat
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.ui.browse.source.SourcesPresenter
import uy.kohesive.injekt.injectLazy
import java.util.Locale

/**
 * Utility class to change the application's language in runtime.
 */
object LocaleHelper {

    private val preferences: PreferencesHelper by injectLazy()

    /**
     * Returns Display name of a string language code
     */
    fun getSourceDisplayName(lang: String?, context: Context): String {
        return when (lang) {
            SourcesPresenter.LAST_USED_KEY -> context.getString(R.string.last_used_source)
            SourcesPresenter.PINNED_KEY -> context.getString(R.string.pinned_sources)
            "other" -> context.getString(R.string.other_source)
            "all" -> context.getString(R.string.all_lang)
            else -> getDisplayName(lang)
        }
    }

    /**
     * Returns Display name of a string language code
     *
     * @param lang empty for system language
     */
    fun getDisplayName(lang: String?): String {
        if (lang == null) {
            return ""
        }

        val locale = if (lang.isEmpty()) {
            LocaleListCompat.getAdjustedDefault()[0]
        } else {
            getLocale(lang)
        }
        return locale!!.getDisplayName(locale).replaceFirstChar { it.uppercase(locale) }
    }

    /**
     * Creates a ContextWrapper using selected Locale
     */
    fun createLocaleWrapper(context: Context): ContextWrapper {
        val appLocale = getLocaleFromString(preferences.lang().get())
        val newConfiguration = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(appLocale)
            newConfiguration.setLocales(localeList)
        } else {
            newConfiguration.setLocale(appLocale)
        }
        Locale.setDefault(appLocale)
        return ContextWrapper(context.createConfigurationContext(newConfiguration))
    }

    /**
     * Return Locale from string language code
     */
    private fun getLocale(lang: String): Locale {
        val sp = lang.split("_", "-")
        return when (sp.size) {
            2 -> Locale(sp[0], sp[1])
            3 -> Locale(sp[0], sp[1], sp[2])
            else -> Locale(lang)
        }
    }

    /**
     * Return English display string from string language code
     */
    fun getSimpleLocaleDisplay(lang: String): String {
        val sp = lang.split("_", "-")
        return Locale(sp[0]).getDisplayLanguage(getLocaleFromString(null))
    }

    /**
     * Returns the locale for the value stored in preferences, defaults to main system language.
     *
     * @param pref the string value stored in preferences.
     */
    private fun getLocaleFromString(pref: String?): Locale {
        if (pref.isNullOrEmpty()) {
            return LocaleListCompat.getDefault()[0]!!
        }
        return getLocale(pref)
    }
}
