package com.pghaz.revery.adapter.spotify

import android.view.ViewGroup
import com.pghaz.revery.adapter.base.BaseAdapter
import com.pghaz.revery.adapter.base.BaseViewHolder
import com.pghaz.revery.adapter.base.ListItemType
import com.pghaz.revery.model.app.spotify.AlbumWrapper
import com.pghaz.revery.model.app.spotify.ArtistWrapper
import com.pghaz.revery.model.app.spotify.PlaylistWrapper
import com.pghaz.revery.model.app.spotify.TrackWrapper

class SpotifyItemsAdapter(private val onSpotifyItemClickListener: OnSpotifyItemClickListener) :
    BaseAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val viewHolder = super.onCreateViewHolder(parent, viewType)

        when (ListItemType.values()[viewType]) {
            ListItemType.SpotifyAlbum,
            ListItemType.SpotifyTrack,
            ListItemType.SpotifyArtist,
            ListItemType.SpotifyPlaylist -> {
                (viewHolder as BaseSpotifyViewHolder).onSpotifyItemClickListener =
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

    override fun getAddedItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is PlaylistWrapper -> ListItemType.SpotifyPlaylist
            is ArtistWrapper -> ListItemType.SpotifyArtist
            is TrackWrapper -> ListItemType.SpotifyTrack
            is AlbumWrapper -> ListItemType.SpotifyAlbum
            else -> ListItemType.Empty
        }.ordinal
    }
}