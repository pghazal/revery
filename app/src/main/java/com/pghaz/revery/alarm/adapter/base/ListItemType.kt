package com.pghaz.revery.alarm.adapter.base

import androidx.annotation.LayoutRes
import com.pghaz.revery.R

enum class ListItemType(@LayoutRes val layoutResId: Int) {
    Empty(R.layout.item_view_empty),
    Alarm(R.layout.item_view_alarm),
    SpotifyPlaylist(R.layout.item_view_playlist),
    SpotifyArtist(R.layout.item_view_artist),
    SpotifyTrack(R.layout.item_view_track),
}