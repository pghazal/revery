package com.pghaz.revery.spotify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SpotifyViewModelFactory(private val accessToken: String?) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SpotifyItemsViewModel::class.java)) {
            return SpotifyItemsViewModel(accessToken) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}