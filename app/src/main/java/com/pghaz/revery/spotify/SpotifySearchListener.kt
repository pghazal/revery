package com.pghaz.revery.spotify

interface SpotifySearchListener {

    fun search(query: String?)

    fun clear()
}