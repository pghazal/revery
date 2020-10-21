package com.pghaz.revery.alarm.adapter.base

import android.view.View
import com.pghaz.revery.alarm.adapter.AlarmViewHolder
import com.pghaz.revery.spotify.adapter.SpotifyArtistViewHolder
import com.pghaz.revery.spotify.adapter.SpotifyPlaylistViewHolder

class ViewHolderFactory {

    companion object {
        fun createViewHolder(listItemType: ListItemType, view: View): BaseViewHolder {
            return when (listItemType) {
                ListItemType.Alarm -> AlarmViewHolder(view)

                ListItemType.SpotifyPlaylist -> SpotifyPlaylistViewHolder(view)
                ListItemType.SpotifyArtist -> SpotifyArtistViewHolder(view)
                else -> EmptyViewHolder(view)
            }
        }
    }
}