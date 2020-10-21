package com.pghaz.revery.spotify.adapter

import com.pghaz.revery.spotify.model.PlaylistWrapper

interface OnSpotifyItemClickListener {
    fun onClick(playlistWrapper: PlaylistWrapper)
}