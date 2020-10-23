package com.pghaz.revery.spotify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SpotifyViewModelFactory(private val accessToken: String, private val filter: SpotifyFilter) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SpotifyItemsViewModel::class.java)) {
            return SpotifyItemsViewModel(accessToken, filter) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}