package com.pghaz.revery.spotify.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pghaz.revery.R
import kaaes.spotify.webapi.android.models.PlaylistSimple

class SpotifyItemsAdapter(private val onSpotifyItemClickListener: OnSpotifyItemClickListener) :
    RecyclerView.Adapter<SpotifyItemViewHolder>() {

    private var items: ArrayList<PlaylistSimple> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpotifyItemViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_view_spotify, parent, false)
        return SpotifyItemViewHolder(view, onSpotifyItemClickListener)
    }

    override fun onBindViewHolder(holder: SpotifyItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun addItems(newItems: List<PlaylistSimple>) {
        this.items.addAll(newItems)
        notifyDataSetChanged()
    }
}