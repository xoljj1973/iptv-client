package com.example.iptv

data class Channel(val name: String, val url: String, val group: String, val tvgId: String?)

object M3uParser {

    private val attrRegex = Regex("([\\w-]+)=\"([^\"]*)\"")

    fun parse(text: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        var pendingName = ""
        var pendingAttrs: Map<String, String> = emptyMap()
        for (raw in text.lineSequence()) {
            val line = raw.trim()
            when {
                line.startsWith("#EXTINF") -> {
                    pendingName = line.substringAfterLast(',').trim()
                    pendingAttrs = attrRegex.findAll(line)
                        .associate { it.groupValues[1].lowercase() to it.groupValues[2] }
                }
                line.isNotEmpty() && !line.startsWith("#") -> {
                    channels += Channel(
                        name = pendingName.ifBlank {
                            line.substringAfterLast('/').substringBefore('.')
                        },
                        url = line,
                        group = pendingAttrs["group-title"]?.takeIf { it.isNotBlank() } ?: "全部",
                        tvgId = pendingAttrs["tvg-id"]?.takeIf { it.isNotBlank() }
                    )
                    pendingName = ""
                    pendingAttrs = emptyMap()
                }
            }
        }
        return channels
    }
}
