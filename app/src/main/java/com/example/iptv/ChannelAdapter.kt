package com.example.iptv

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChannelAdapter(private val onClick: (Channel) -> Unit) :
    RecyclerView.Adapter<ChannelAdapter.VH>() {

    private var items: List<Channel> = emptyList()

    fun submit(list: List<Channel>) {
        items = list
        notifyDataSetChanged()
    }

    fun notifyDataChanged() {
        notifyDataSetChanged()
    }

    class VH(val title: TextView, val sub: TextView) : RecyclerView.ViewHolder(title)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        val t = v.findViewById<TextView>(android.R.id.text1)
        val s = v.findViewById<TextView>(android.R.id.text2)
        v.setPadding(32, 24, 32, 24)
        return VH(t, s)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ch = items[position]
        holder.title.text = ch.name
        holder.sub.text = ChannelStore.nowPlaying(ch)?.title ?: ch.group
        holder.title.setOnClickListener { onClick(ch) }
    }

    override fun getItemCount() = items.size
}
