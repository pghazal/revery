package com.pghaz.revery.viewmodel.spotify

import io.github.kaaes.spotify.webapi.retrofit.v2.SpotifyError

interface SpotifyErrorListener {

    fun onSpotifyError(error: SpotifyError)
}