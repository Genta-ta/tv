package id.fc.pratv.data.remote

import id.fc.pratv.data.model.Channel
import org.json.JSONObject

object M3UParser {

    fun parse(text: String, onProgress: (Float) -> Unit = {}): List<Channel> {
        val channels = mutableListOf<Channel>()
        var pendingName: String? = null
        var pendingId: String? = null
        var pendingLogo: String? = null
        var pendingGroup: String? = null
        val headers = mutableMapOf<String, String>()
        var drmType: String? = null
        var drmKeyId: String? = null
        var drmKey: String? = null
        var drmLicenseUri: String? = null

        fun reset() {
            pendingName = null
            pendingId = null
            pendingLogo = null
            pendingGroup = null
            headers.clear()
            drmType = null
            drmKeyId = null
            drmKey = null
            drmLicenseUri = null
        }

        val totalLines = text.lineSequence().count()
        var lineNo = 0
        for (raw in text.lineSequence()) {
            val line = raw.trim()
            if (line.isEmpty()) continue
            lineNo++
            if (totalLines > 0 && lineNo % 500 == 0) {
                onProgress((lineNo.toFloat() / totalLines).coerceIn(0f, 1f))
            }

            when {
                line.startsWith("#EXTINF") -> {
                    val comma = line.lastIndexOf(',')
                    pendingName = if (comma >= 0) line.substring(comma + 1).trim() else ""
                    val attrs = if (comma >= 0) line.substring(0, comma) else line
                    pendingId = attr(attrs, "tvg-id")
                    pendingLogo = attr(attrs, "tvg-logo")
                    pendingGroup = attr(attrs, "group-title")
                }
                line.startsWith("#EXTVLCOPT") -> {
                    val eq = line.indexOf('=')
                    if (eq >= 0) {
                        val key = line.substring("#EXTVLCOPT:".length, eq).trim().lowercase()
                        val value = line.substring(eq + 1).trim()
                        when (key) {
                            "http-user-agent" -> headers["user-agent"] = value
                            "http-referrer" -> headers["referer"] = value
                            "http-origin" -> headers["origin"] = value
                        }
                    }
                }
                line.startsWith("#KODIPROP") -> {
                    val eq = line.indexOf('=')
                    if (eq >= 0) {
                        val key = line.substring("#KODIPROP:".length, eq).trim()
                        val value = line.substring(eq + 1).trim()
                        when (key) {
                            "inputstream.adaptive.license_type" -> drmType = value
                            "inputstream.adaptive.license_key" -> applyLicenseKey(value, drmType) { kid, k ->
                                drmKeyId = kid
                                drmKey = k
                            }?.let { uri -> drmLicenseUri = uri }
                            "inputstream.adaptive.stream_headers" -> value.split("|").forEach { pair ->
                                val p = pair.split("=", limit = 2)
                                if (p.size == 2) applyHeader(p[0].trim().lowercase(), p[1].trim(), headers)
                            }
                            else -> {
                                if (key.lowercase().startsWith("http")) {
                                    val sub = key.lowercase().removePrefix("http")
                                    applyHeader(sub, value, headers)
                                }
                            }
                        }
                    }
                }
                line.startsWith("#EXTHTTP") -> {
                    val jsonStart = line.indexOf('{')
                    if (jsonStart >= 0) {
                        try {
                            val obj = JSONObject(line.substring(jsonStart))
                            obj.keys().forEach { k ->
                                applyHeader(k.lowercase(), obj.getString(k), headers)
                            }
                        } catch (_: Exception) {
                        }
                    }
                }
                line.startsWith("#") -> Unit
                else -> {
                    if (pendingName != null) {
                        val (cleanUrl, embeddedType, embeddedKey) = splitEmbeddedDrm(line)
                        if (embeddedType != null) drmType = embeddedType
                        if (embeddedType != null && embeddedKey != null) {
                            applyLicenseKey(embeddedKey, drmType) { kid, k ->
                                drmKeyId = kid
                                drmKey = k
                            }?.let { uri -> drmLicenseUri = uri }
                        }
                        channels.add(
                            Channel(
                                id = pendingId,
                                name = pendingName!!,
                                logoUrl = pendingLogo,
                                group = pendingGroup?.ifBlank { null },
                                url = cleanUrl,
                                drmType = drmType,
                                drmKeyId = drmKeyId,
                                drmKey = drmKey,
                                drmLicenseUri = drmLicenseUri,
                                userAgent = headers["user-agent"],
                                referer = headers["referer"],
                                origin = headers["origin"],
                                extraHeaders = headers
                                    .filterKeys { it !in setOf("user-agent", "referer", "origin") }
                            )
                        )
                    }
                    reset()
                }
            }
        }
        onProgress(1f)
        return channels
    }

    private fun applyHeader(key: String, value: String, headers: MutableMap<String, String>) {
        when (key) {
            "user-agent" -> headers["user-agent"] = value
            "referer", "referrer" -> headers["referer"] = value
            "origin" -> headers["origin"] = value
            else -> headers[key] = value
        }
    }

    private fun applyLicenseKey(
        value: String,
        drmType: String?,
        setClearKey: (kid: String, key: String) -> Unit
    ): String? {
        if (drmType?.contains("widevine", true) == true) return value
        if (value.startsWith("{")) {
            try {
                val obj = JSONObject(value)
                val it = obj.keys()
                if (it.hasNext()) {
                    val k = it.next()
                    val v = obj.getString(k)
                    if (drmType?.contains("widevine", true) == true) return v
                    setClearKey(k, v)
                }
            } catch (_: Exception) {
            }
            return null
        }
        val idx = value.indexOf(':')
        if (idx >= 0) {
            setClearKey(value.substring(0, idx), value.substring(idx + 1))
        }
        return null
    }

    private fun attr(attrPart: String, name: String): String? {
        val m = Regex("""$name="([^"]*)"""").find(attrPart) ?: return null
        return m.groupValues[1].ifBlank { null }
    }

    fun parseEpgUrl(text: String): String? {
        val m = Regex("""#EXTM3U[^#]*url-tvg="([^"]*)"""").find(text)
        return m?.groupValues?.get(1)?.ifBlank { null }
    }

    private fun splitEmbeddedDrm(rawUrl: String): Triple<String, String?, String?> {
        val bar = rawUrl.indexOf('|')
        if (bar < 0) return Triple(rawUrl, null, null)
        val url = rawUrl.substring(0, bar)
        val query = rawUrl.substring(bar + 1)
        val type = Regex("""license_type=([^&]+)""").find(query)?.groupValues?.get(1)?.let {
            runCatching { java.net.URLDecoder.decode(it, "UTF-8") }.getOrDefault(it)
        }
        val key = Regex("""license_key=([^&]+)""").find(query)?.groupValues?.get(1)?.let {
            runCatching { java.net.URLDecoder.decode(it, "UTF-8") }.getOrDefault(it)
        }
        return Triple(url, type, key)
    }
}
