package id.fc.pratv.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import id.fc.pratv.data.repository.PlaylistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SettingsStore {
    private const val NAME = "pratv_prefs"
    private const val KEY_URLS = "playlist_urls"
    private const val KEY_ACTIVE_URL = "playlist_active"
    private const val KEY_URL_LEGACY = "playlist_url"
    private const val KEY_SHOW_LOG = "show_log"
    private const val KEY_AUTO_SKIP = "auto_skip"
    private const val KEY_THEME = "theme"

    val PRESET_URLS = listOf(
        PlaylistRepository.DEFAULT_URL,
        PlaylistRepository.OTT_URL
    )

    fun isPreset(url: String): Boolean = url in PRESET_URLS

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    private val _themeKey = MutableStateFlow("gelap")
    val themeKeyFlow: StateFlow<String> = _themeKey.asStateFlow()

    fun getUrls(context: Context): List<String> {
        val allPrefs = prefs(context)
        val legacy = allPrefs.getString(KEY_URL_LEGACY, null)
        val joined = allPrefs.getString(KEY_URLS, null)
        val custom = if (joined.isNullOrBlank()) {
            emptyList()
        } else {
            joined.split("|").filter { it.isNotBlank() && it !in PRESET_URLS }
        }
        val list = PRESET_URLS + custom + (legacy?.takeIf { it !in PRESET_URLS }?.let { listOf(it) } ?: emptyList())
        if (legacy != null) {
            allPrefs.edit { remove(KEY_URL_LEGACY) }
        }
        return list.distinct()
    }

    fun getActiveUrl(context: Context): String {
        val allPrefs = prefs(context)
        val active = allPrefs.getString(KEY_ACTIVE_URL, null)
        val urls = getUrls(context)
        return active?.takeIf { it in urls } ?: urls.firstOrNull()
            ?: PlaylistRepository.DEFAULT_URL
    }

    fun addUrl(context: Context, url: String) {
        val trimmed = url.trim()
        if (trimmed.isBlank()) return
        val urls = getUrls(context).toMutableList()
        if (trimmed !in urls) urls.add(trimmed)
        prefs(context).edit {
            putString(KEY_URLS, urls.joinToString("|"))
        }
    }

    fun removeUrl(context: Context, url: String) {
        if (isPreset(url)) return
        val urls = getUrls(context).toMutableList()
        urls.remove(url)
        val active = getActiveUrl(context)
        prefs(context).edit {
            if (urls.isEmpty()) {
                putString(KEY_URLS, PlaylistRepository.DEFAULT_URL)
            } else {
                putString(KEY_URLS, urls.joinToString("|"))
            }
            if (active == url && urls.isNotEmpty()) {
                putString(KEY_ACTIVE_URL, urls.first())
            }
        }
    }

    fun setActiveUrl(context: Context, url: String) {
        prefs(context).edit {
            putString(KEY_ACTIVE_URL, url)
        }
    }

    fun getShowLog(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SHOW_LOG, false)

    fun setShowLog(context: Context, value: Boolean) {
        prefs(context).edit { putBoolean(KEY_SHOW_LOG, value) }
    }

    fun getAutoSkip(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AUTO_SKIP, false)

    fun setAutoSkip(context: Context, value: Boolean) {
        prefs(context).edit { putBoolean(KEY_AUTO_SKIP, value) }
    }

    fun getTheme(context: Context): String =
        prefs(context).getString(KEY_THEME, "gelap") ?: "gelap"

    fun setTheme(context: Context, theme: String) {
        prefs(context).edit { putString(KEY_THEME, theme) }
        _themeKey.value = theme
    }
}
