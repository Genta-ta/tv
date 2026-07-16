package id.fc.pratv.data.remote

import android.util.Log
import id.fc.pratv.data.AppLogger
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object EpgParser {

    data class Programme(
        val startMs: Long,
        val stopMs: Long,
        val title: String,
        val desc: String
    )

    fun parse(xml: String): Map<String, List<Programme>> {
        val result = mutableMapOf<String, MutableList<Programme>>()
        var lastProgrammeTag = ""
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var currentChannel: String? = null
            var inProgramme = false
            var programmeChannel: String? = null
            var startMs = 0L
            var stopMs = 0L
            var title = ""
            var desc = ""

            var event = parser.next()
            while (event != XmlPullParser.END_DOCUMENT) {
                when (event) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "channel" -> currentChannel = parser.getAttributeValue(null, "id")
                            "programme" -> {
                                inProgramme = true
                                programmeChannel = parser.getAttributeValue(null, "channel")
                                startMs = parseXmlTvTime(parser.getAttributeValue(null, "start"))
                                stopMs = parseXmlTvTime(parser.getAttributeValue(null, "stop"))
                                title = ""
                                desc = ""
                                lastProgrammeTag = ""
                            }
                            "title", "desc", "sub-title" -> {
                                if (inProgramme) lastProgrammeTag = parser.name
                            }
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inProgramme && lastProgrammeTag.isNotEmpty()) {
                            val text = parser.text.orEmpty().trim()
                            when (lastProgrammeTag) {
                                "title" -> if (title.isEmpty()) title = text
                                "desc", "sub-title" -> if (desc.isEmpty()) desc = text
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (parser.name) {
                            "title", "desc", "sub-title" -> lastProgrammeTag = ""
                            "programme" -> {
                                val ch = programmeChannel ?: currentChannel
                                if (ch != null && startMs > 0 && title.isNotEmpty()) {
                                    val list = result.getOrPut(ch) { mutableListOf() }
                                    list.add(Programme(startMs, stopMs, title, desc))
                                }
                                inProgramme = false
                                lastProgrammeTag = ""
                            }
                        }
                    }
                }
                event = parser.next()
            }
            result.values.forEach { it.sortBy { p -> p.startMs } }
        } catch (e: Exception) {
            Log.e("EpgParser", "parse failed: ${e.message}")
            AppLogger.e("EpgParser", "parse failed: ${e.message}")
        }
        return result
    }

    fun parseXmlTvTime(value: String?): Long {
        if (value.isNullOrEmpty()) return 0L
        val tzIdx = value.indexOf(' ')
        val timePart = if (tzIdx >= 0) value.substring(0, tzIdx) else value
        val tzPart = if (tzIdx >= 0) value.substring(tzIdx + 1) else ""
        if (timePart.length < 14) return 0L
        val sdf = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val base = sdf.parse(timePart)?.time ?: return 0L
        val offsetMs = parseOffset(tzPart)
        return base - offsetMs
    }

    private fun parseOffset(tz: String): Long {
        if (tz.length < 5) return 0L
        val sign = if (tz.startsWith('-')) -1 else 1
        val digits = tz.replace("[+\\-]".toRegex(), "")
        if (digits.length < 4) return 0L
        val hours = digits.substring(0, 2).toIntOrNull() ?: 0
        val minutes = digits.substring(2, 4).toIntOrNull() ?: 0
        return sign * (hours * 3600L + minutes * 60L) * 1000L
    }
}
