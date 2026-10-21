package com.example.iptv

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.Reader
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** Minimal XMLTV (xmltv.dtd) programme guide parser. */
object EpgParser {

    data class Programme(
        val channelId: String,
        val title: String,
        val startMs: Long,
        val stopMs: Long
    )

    fun parse(reader: Reader): Map<String, List<Programme>> {
        val parser = XmlPullParserFactory.newInstance().newPullParser()
        parser.setInput(reader)
        val byChannel = HashMap<String, MutableList<Programme>>()
        var channel = ""
        var title = ""
        var start = 0L
        var stop = 0L
        var inTitle = false
        val fmt = SimpleDateFormat("yyyyMMddHHmmss Z", Locale.US)

        var ev = parser.eventType
        while (ev != XmlPullParser.END_DOCUMENT) {
            when (ev) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "programme" -> {
                        channel = parser.getAttributeValue(null, "channel") ?: ""
                        start = parseTime(parser.getAttributeValue(null, "start"), fmt)
                        stop = parseTime(parser.getAttributeValue(null, "stop"), fmt)
                    }
                    "title" -> inTitle = true
                }
                XmlPullParser.TEXT -> if (inTitle) title = parser.text.trim()
                XmlPullParser.END_TAG -> when (parser.name) {
                    "title" -> inTitle = false
                    "programme" -> if (channel.isNotEmpty()) {
                        byChannel.getOrPut(channel) { mutableListOf() } +=
                            Programme(channel, title, start, stop)
                    }
                }
            }
            ev = parser.next()
        }
        return byChannel
    }

    private fun parseTime(raw: String?, fmt: SimpleDateFormat): Long {
        if (raw.isNullOrBlank()) return 0
        return try {
            // XMLTV: 20241021080000 +0800 or 20241021080000
            val norm = if (raw.length == 14) "$raw +0000" else raw
            fmt.parse(norm)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}
