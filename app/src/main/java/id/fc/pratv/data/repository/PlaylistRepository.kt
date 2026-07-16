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

    suspend fun getChannels(
        context: Context,
        force: Boolean = false,
        onProgress: (Float) -> Unit = {}
    ): List<Channel> {
        if (!force && cache != null) return cache!!
        val url = SettingsStore.getActiveUrl(context)
        AppLogger.i("Playlist", "getChannels force=$force url=$url")
        val text = fetchText(url, onProgress)
        val parsed = M3UParser.parse(text)
        AppLogger.i("Playlist", "parsed ${parsed.size} channels")
        cache = parsed
        return parsed
    }

    suspend fun getEpg(
        context: Context,
        force: Boolean = false,
        onProgress: (Float) -> Unit = {}
    ): Map<String, List<EpgParser.Programme>> {
        if (!force && epgCache != null) return epgCache!!
        val url = SettingsStore.getActiveUrl(context)
        AppLogger.i("Epg", "getEpg force=$force url=$url")
        val playlistText = fetchText(url)
        val epgUrl = M3UParser.parseEpgUrl(playlistText) ?: run {
            AppLogger.w("Epg", "no url-tvg found")
            return emptyMap()
        }
        AppLogger.i("Epg", "epg url=$epgUrl")
        val epgText = fetchText(epgUrl, onProgress)
        val parsed = EpgParser.parse(epgText)
        AppLogger.i("Epg", "parsed ${parsed.size} channels / ${parsed.values.sumOf { it.size }} programmes")
        epgCache = parsed
        return parsed
    }

    private suspend fun fetchText(url: String, onProgress: (Float) -> Unit = {}): String =
        withContext(Dispatchers.IO) {
            AppLogger.d("Fetch", "GET $url")
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("User-Agent", "Mozilla/5.0")
            }
            try {
                val total = conn.contentLengthLong.coerceAtLeast(0L)
                val sb = StringBuilder()
                conn.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                    val buf = CharArray(8192)
                    var read: Int
                    var soFar = 0L
                    var lastEmit = -1f
                    var nextEmitBytes = 0L
                    while (reader.read(buf).also { read = it } != -1) {
                        sb.append(buf, 0, read)
                        soFar += read
                        val frac = if (total > 0) {
                            (soFar.toFloat() / total).coerceIn(0f, 0.99f)
                        } else {
                            // ukuran tak diketahui (chunked/gzip tanpa Content-Length):
                            // rayap halus 0 -> ~0.95 berbasis byte + Estimasi membesar
                            val est = (soFar * 2L).coerceAtLeast(1L)
                            (soFar.toFloat() / est).coerceIn(0f, 0.95f)
                        }
                        // coalesce: hanya emit bila delta >= 1% ATAU tiap ~64KB agar tak spam StateFlow
                        val delta = if (lastEmit < 0f) 1f else kotlin.math.abs(frac - lastEmit)
                        if (delta >= 0.01f || soFar >= nextEmitBytes) {
                            onProgress(frac)
                            lastEmit = frac
                            nextEmitBytes = soFar + 65536L
                        }
                    }
                }
                onProgress(1f)
                sb.toString()
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
