package com.pghaz.revery.spotify.adapter

import kaaes.spotify.webapi.android.models.PlaylistSimple

interface OnSpotifyItemClickListener {
    fun onClick(playlist: PlaylistSimple)
}