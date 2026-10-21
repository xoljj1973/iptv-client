package com.example.iptv

object ChannelStore {
    var channels: List<Channel> = emptyList()
    var epg: Map<String, List<EpgParser.Programme>> = emptyMap()
    var current: Int = 0

    fun nowPlaying(ch: Channel): EpgParser.Programme? {
        val id = ch.tvgId ?: ch.name
        val now = System.currentTimeMillis()
        return epg[id]?.firstOrNull { now in it.startMs until it.stopMs }
    }
}
