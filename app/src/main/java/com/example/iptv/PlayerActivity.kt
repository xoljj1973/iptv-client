package com.example.iptv

import android.os.Bundle
import android.view.KeyEvent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView

class PlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        index = ChannelStore.current
        playCurrent()
    }

    private fun playCurrent() {
        val ch = ChannelStore.channels.getOrNull(index) ?: return
        findViewById<TextView>(R.id.channel_name).text = ch.name
        findViewById<TextView>(R.id.now_playing).text =
            ChannelStore.nowPlaying(ch)?.title ?: ""
        val factory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(10_000)
        player?.release()
        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(factory))
            .build()
        findViewById<PlayerView>(R.id.surface).player = player
        val item = MediaItem.fromUri(Util.parseUri(ch.url))
        if (ch.url.contains(".m3u8")) {
            player!!.setMediaSource(HlsMediaSource.Factory(factory).createMediaSource(item))
        } else {
            player!!.setMediaItem(item)
        }
        player!!.prepare()
        player!!.playWhenReady = true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_CHANNEL_UP -> { next(1); return true }
            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_CHANNEL_DOWN -> { next(-1); return true }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun next(delta: Int) {
        if (ChannelStore.channels.isEmpty()) return
        index = (index + delta + ChannelStore.channels.size) % ChannelStore.channels.size
        ChannelStore.current = index
        playCurrent()
    }

    override fun onStop() {
        super.onStop()
        player?.playWhenReady = false
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
