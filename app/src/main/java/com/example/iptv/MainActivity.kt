package com.example.iptv

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.StringReader
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var list: RecyclerView
    private lateinit var status: TextView
    private lateinit var adapter: ChannelAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        list = findViewById(R.id.channel_list)
        status = findViewById(R.id.status)
        adapter = ChannelAdapter { ch ->
            ChannelStore.current = ChannelStore.channels.indexOf(ch)
            startActivity(Intent(this, PlayerActivity::class.java))
        }
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter
        findViewById<View>(R.id.btn_source).setOnClickListener { askUrl("m3u_url", R.string.set_source) }
        findViewById<View>(R.id.btn_epg).setOnClickListener { askUrl("epg_url", R.string.set_epg) }
        load()
    }

    private fun load() {
        val prefs = getSharedPreferences("cfg", MODE_PRIVATE)
        val m3u = prefs.getString("m3u_url", null)
        if (m3u.isNullOrBlank()) {
            status.text = getString(R.string.hint_set_source)
            return
        }
        status.text = getString(R.string.loading)
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val text = withContext(Dispatchers.IO) { URL(m3u).readText() }
                ChannelStore.channels = M3uParser.parse(text)
                adapter.submit(ChannelStore.channels)
                status.text = getString(R.string.loaded_n, ChannelStore.channels.size)
                val epgUrl = prefs.getString("epg_url", null)
                if (!epgUrl.isNullOrBlank()) loadEpg(epgUrl)
            } catch (e: Exception) {
                status.text = getString(R.string.load_failed, e.message ?: "?")
            }
        }
    }

    private fun loadEpg(url: String) {
        status.text = getString(R.string.loading_epg)
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val xml = withContext(Dispatchers.IO) { URL(url).readText() }
                ChannelStore.epg = withContext(Dispatchers.Default) {
                    EpgParser.parse(StringReader(xml))
                }
                adapter.notifyDataChanged()
                status.text = getString(R.string.loaded_n, ChannelStore.channels.size)
            } catch (e: Exception) {
                status.text = getString(R.string.epg_failed, e.message ?: "?")
            }
        }
    }

    private fun askUrl(key: String, titleRes: Int) {
        val prefs = getSharedPreferences("cfg", MODE_PRIVATE)
        val input = EditText(this).apply { setText(prefs.getString(key, "")) }
        AlertDialog.Builder(this)
            .setTitle(titleRes)
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                prefs.edit { putString(key, input.text.toString().trim()) }
                load()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
