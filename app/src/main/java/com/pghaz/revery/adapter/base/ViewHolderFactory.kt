package com.pghaz.revery.adapter.base

import android.view.View
import com.pghaz.revery.adapter.alarm.AlarmViewHolder
import com.pghaz.revery.adapter.spotify.SpotifyAlbumViewHolder
import com.pghaz.revery.adapter.spotify.SpotifyArtistViewHolder
import com.pghaz.revery.adapter.spotify.SpotifyPlaylistViewHolder
import com.pghaz.revery.adapter.spotify.SpotifyTrackViewHolder
import com.pghaz.revery.adapter.timer.TimerViewHolder

class ViewHolderFactory {

    companion object {
        fun createViewHolder(listItemType: ListItemType, view: View): BaseViewHolder {
            return when (listItemType) {
                ListItemType.Alarm -> AlarmViewHolder(view)
                ListItemType.Timer -> TimerViewHolder(view)

                ListItemType.SpotifyPlaylist -> SpotifyPlaylistViewHolder(view)
                ListItemType.SpotifyArtist -> SpotifyArtistViewHolder(view)
                ListItemType.SpotifyTrack -> SpotifyTrackViewHolder(view)
                ListItemType.SpotifyAlbum -> SpotifyAlbumViewHolder(view)
                else -> EmptyViewHolder(view)
            }
        }
    }
}