package com.pghaz.revery.viewmodel.spotify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pghaz.revery.model.app.spotify.SpotifyFilter

class SpotifyViewModelFactory(private val accessToken: String, private val filter: SpotifyFilter) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SpotifyItemsViewModel::class.java)) {
            return SpotifyItemsViewModel(accessToken, filter) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}