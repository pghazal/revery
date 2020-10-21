package com.pghaz.revery.spotify.adapter

import android.view.ViewGroup
import com.pghaz.revery.alarm.adapter.base.BaseAdapter
import com.pghaz.revery.alarm.adapter.base.BaseViewHolder
import com.pghaz.revery.alarm.adapter.base.ListItemType

class SpotifyItemsAdapter(private val onSpotifyItemClickListener: OnSpotifyItemClickListener) :
    BaseAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val viewHolder = super.onCreateViewHolder(parent, viewType)

        when (ListItemType.values()[viewType]) {
            ListItemType.Spotify -> {
                (viewHolder as SpotifyItemViewHolder).onSpotifyItemClickListener =
                    onSpotifyItemClickListener
            }
            else -> {
                // do nothing for now
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}