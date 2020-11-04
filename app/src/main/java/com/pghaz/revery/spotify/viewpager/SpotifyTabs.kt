package com.pghaz.revery.spotify.viewpager

import androidx.annotation.StringRes
import com.pghaz.revery.R
import com.pghaz.revery.model.app.spotify.SpotifyFilter

enum class SpotifyTabs(@StringRes val textResId: Int, val filter: SpotifyFilter) {
    RECENTLY_PLAYED(R.string.recently_played, SpotifyFilter.RECENTLY_PLAYED),
    MY_PLAYLISTS(R.string.my_playlists, SpotifyFilter.MY_PLAYLISTS),
    MY_SAVED_ALBUMS(R.string.my_saved_albums, SpotifyFilter.MY_SAVED_ALBUMS),
    MY_SAVED_TRACKS(R.string.my_saved_tracks, SpotifyFilter.MY_SAVED_TRACKS),
    MY_TOP_ARTISTS(R.string.my_top_artists, SpotifyFilter.MY_TOP_ARTISTS),
    MY_TOP_TRACKS(R.string.my_top_tracks, SpotifyFilter.MY_TOP_TRACKS),
}