package com.pghaz.revery.adapter.base

import androidx.annotation.LayoutRes
import com.pghaz.revery.R

enum class ListItemType(@LayoutRes val layoutResId: Int) {
    Empty(R.layout.item_view_empty),
    Alarm(R.layout.item_view_alarm),
    SpotifyPlaylist(R.layout.item_view_media_square),
    SpotifyArtist(R.layout.item_view_media_round),
    SpotifyTrack(R.layout.item_view_media_square),
    SpotifyAlbum(R.layout.item_view_media_square),
}