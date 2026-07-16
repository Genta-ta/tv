package id.fc.pratv.ui.player

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import id.fc.pratv.data.AppLogger
import id.fc.pratv.data.model.Channel
import id.fc.pratv.data.remote.ClearKeyUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException

private const val TAG = "PratvPlayer"

class PlayerViewModel(app: Application) : AndroidViewModel(app) {

    private val _player = MutableStateFlow<ExoPlayer?>(null)
    val player: StateFlow<ExoPlayer?> = _player.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _diagnostic = MutableStateFlow<String?>(null)
    val diagnostic: StateFlow<String?> = _diagnostic.asStateFlow()

    private val _retrying = MutableStateFlow(false)
    val retrying: StateFlow<Boolean> = _retrying.asStateFlow()

    @UnstableApi
    fun prepare(channel: Channel) {
        val context = getApplication<Application>()
        val diag = StringBuilder()
        try {
            val headers = mutableMapOf<String, String>()
            headers["User-Agent"] = channel.userAgent ?: "Mozilla/5.0 (Linux; Android) ExoPlayer"
            channel.referer?.let { headers["Referer"] = it }
            channel.origin?.let { headers["Origin"] = it }
            if (channel.referer == null) {
                runCatching {
                    val host = java.net.URI(channel.url).host
                    if (host != null) {
                        headers["Referer"] = "https://$host/"
                        if (channel.origin == null) headers["Origin"] = "https://$host"
                    }
                }
            }
            channel.extraHeaders.forEach { (k, v) -> headers.put(k, v) }

            diag.appendLine("Channel     : ${channel.name}")
            diag.appendLine("URL         : ${channel.url}")
            diag.appendLine("Group       : ${channel.group}")
            diag.appendLine("Headers     : ${headers.entries.joinToString { "${it.key}=${it.value}" }}")
            diag.appendLine(
                "DRM         : " + when {
                    channel.drmType?.contains("widevine", true) == true ->
                        "widevine (licenseUri=${channel.drmLicenseUri ?: "-"})"
                    channel.drmType?.contains("clearkey", true) == true ->
                        "clearkey (keyId=${channel.drmKeyId ?: "-"}, key=${channel.drmKey ?: "-"})"
                    else -> "none"
                }
            )

            val drm: MediaItem.DrmConfiguration? = when {
                channel.drmType?.contains("widevine", true) == true && channel.drmLicenseUri != null ->
                    MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                        .setLicenseUri(channel.drmLicenseUri)
                        .apply { if (headers.isNotEmpty()) setLicenseRequestHeaders(headers) }
                        .build()
                channel.drmType?.contains("clearkey", true) == true &&
                    channel.drmKeyId != null && channel.drmKey != null ->
                    MediaItem.DrmConfiguration.Builder(C.CLEARKEY_UUID)
                        .setKeySetId(
                            ClearKeyUtils.buildClearKeyJson(channel.drmKeyId, channel.drmKey)
                                .toByteArray(Charsets.UTF_8)
                        )
                        .apply { if (headers.isNotEmpty()) setLicenseRequestHeaders(headers) }
                        .build()
                else -> null
            }

            val mediaItem = MediaItem.Builder()
                .setUri(channel.url)
                .apply { drm?.let { setDrmConfiguration(it) } }
                .build()

            val httpFactory = DefaultHttpDataSource.Factory()
            httpFactory.setDefaultRequestProperties(headers)
            httpFactory.setUserAgent(headers["User-Agent"])
            val mediaSourceFactory = DefaultMediaSourceFactory(context)
                .setDataSourceFactory(httpFactory)

            Log.d(TAG, "prepare: ${channel.name} url=${channel.url} drm=${channel.drmType} headers=$headers")
            AppLogger.d(TAG, "prepare ${channel.name} | ${channel.url} | drm=${channel.drmType}")

            val exoPlayer = ExoPlayer.Builder(context)
                .setMediaSourceFactory(mediaSourceFactory)
                .build()
                .apply {
                    setMediaItem(mediaItem)
                    prepare()
                    playWhenReady = true
                    addListener(object : Player.Listener {
                        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                            val msg = error.message ?: "Gagal memutar channel"
                            _error.value = msg
                            _diagnostic.value = buildDiagnostic(diag, error)
                            Log.e(TAG, "onPlayerError: ${channel.name} -> $msg", error)
                            AppLogger.e(TAG, "onPlayerError ${channel.name}: $msg")
                        }

                        override fun onPlayerErrorChanged(error: androidx.media3.common.PlaybackException?) {
                            error ?: return
                            val msg = error.message ?: "Gagal memutar channel"
                            _error.value = msg
                            _diagnostic.value = buildDiagnostic(diag, error)
                            Log.e(TAG, "onPlayerErrorChanged: ${channel.name} -> $msg", error)
                            AppLogger.e(TAG, "onPlayerErrorChanged ${channel.name}: $msg")
                        }
                    })
                    addAnalyticsListener(object : AnalyticsListener {
                        override fun onLoadError(
                            eventTime: AnalyticsListener.EventTime,
                            loadEventInfo: LoadEventInfo,
                            mediaLoadData: MediaLoadData,
                            error: java.io.IOException,
                            wasCanceled: Boolean
                        ) {
                            var t: Throwable? = error
                            var status = -1
                            while (t != null) {
                                if (t is androidx.media3.datasource.HttpDataSource.InvalidResponseCodeException) {
                                    status = t.responseCode
                                }
                                t = t.cause
                            }
                            val cause = error.cause
                            val base = when {
                                cause is java.io.IOException -> cause.message ?: cause.javaClass.simpleName
                                else -> error.message ?: error.javaClass.simpleName
                            }
                            val reason = when {
                                status == 403 -> "Channel dilindungi (HTTP 403) — butuh player ber-DRM/resmi"
                                status > 0 -> "HTTP $status - $base"
                                base.contains("timeout", true) -> "Channel timeout — mungkin dibatasi region/server"
                                else -> base
                            }
                            Log.e(TAG, "onLoadError: ${channel.name} -> $reason (status=$status)", error)
                            AppLogger.e(TAG, "onLoadError ${channel.name}: $reason (status=$status)")
                            _error.value = reason
                            _diagnostic.value = buildDiagnostic(diag, error)
                        }
                    })
                }

            _player.value?.release()
            _player.value = exoPlayer
        } catch (e: Exception) {
            val msg = e.message ?: "Gagal memutar channel"
            _error.value = msg
            _diagnostic.value = buildDiagnostic(diag, e)
            Log.e(TAG, "prepare failed: ${channel.name}", e)
            AppLogger.e(TAG, "prepare failed ${channel.name}: ${e.message}")
        }
    }

    private fun buildDiagnostic(base: StringBuilder, throwable: Throwable?): String {
        val sb = StringBuilder(base.toString())
        sb.appendLine("----")
        sb.appendLine("Error       : ${throwable?.message ?: "unknown"}")
        val cause = throwable?.cause
        if (cause != null) {
            sb.appendLine("Cause       : ${cause.javaClass.simpleName}: ${cause.message}")
        }
        sb.appendLine("Type        : ${throwable?.javaClass?.simpleName ?: "-"}")
        return sb.toString().trimEnd()
    }

    fun retry(channel: Channel) {
        _error.value = null
        _diagnostic.value = null
        _player.value?.release()
        _player.value = null
        prepare(channel)
    }

    fun release() {
        _player.value?.release()
        _player.value = null
        _error.value = null
        _diagnostic.value = null
    }

    override fun onCleared() {
        _player.value?.release()
        _player.value = null
    }
}
