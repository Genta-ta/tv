package id.fc.pratv.data.repository

import android.content.Context
import id.fc.pratv.data.AppLogger
import id.fc.pratv.data.SettingsStore
import id.fc.pratv.data.model.Channel
import id.fc.pratv.data.remote.EpgParser
import id.fc.pratv.data.remote.M3UParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object PlaylistRepository {

    const val DEFAULT_URL =
        "https://raw.githubusercontent.com/dhasap/dhanytv/main/dhanytv.m3u"

    const val OTT_URL =
        "https://raw.githubusercontent.com/dhasap/dhanytv/main/dhanytv-ott.m3u"

    private var cache: List<Channel>? = null
    private var epgCache: Map<String, List<EpgParser.Programme>>? = null

    suspend fun getChannels(context: Context, force: Boolean = false): List<Channel> {
        if (!force && cache != null) return cache!!
        val url = SettingsStore.getActiveUrl(context)
        AppLogger.i("Playlist", "getChannels force=$force url=$url")
        val text = fetchText(url)
        val parsed = M3UParser.parse(text)
        AppLogger.i("Playlist", "parsed ${parsed.size} channels")
        cache = parsed
        return parsed
    }

    suspend fun getEpg(context: Context, force: Boolean = false): Map<String, List<EpgParser.Programme>> {
        if (!force && epgCache != null) return epgCache!!
        val url = SettingsStore.getActiveUrl(context)
        AppLogger.i("Epg", "getEpg force=$force url=$url")
        val playlistText = fetchText(url)
        val epgUrl = M3UParser.parseEpgUrl(playlistText) ?: run {
            AppLogger.w("Epg", "no url-tvg found")
            return emptyMap()
        }
        AppLogger.i("Epg", "epg url=$epgUrl")
        val epgText = fetchText(epgUrl)
        val parsed = EpgParser.parse(epgText)
        AppLogger.i("Epg", "parsed ${parsed.size} channels / ${parsed.values.sumOf { it.size }} programmes")
        epgCache = parsed
        return parsed
    }

    private suspend fun fetchText(url: String): String = withContext(Dispatchers.IO) {
        AppLogger.d("Fetch", "GET $url")
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15000
            readTimeout = 15000
            setRequestProperty("User-Agent", "Mozilla/5.0")
        }
        try {
            conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } catch (e: Exception) {
            AppLogger.e("Fetch", "GET $url failed: ${e.message}")
            throw e
        }
    }

    fun clearCache() {
        cache = null
        epgCache = null
    }
}
