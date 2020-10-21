package com.pghaz.revery.spotify.adapter

import android.view.View
import com.pghaz.revery.alarm.model.BaseModel
import com.pghaz.revery.image.ImageLoader
import com.pghaz.revery.spotify.model.ArtistWrapper

class SpotifyArtistViewHolder(view: View) : BaseSpotifyViewHolder(view) {

    override fun bind(model: BaseModel) {
        super.bind(model)
        val wrapper = model as ArtistWrapper

        titleTextView.text = wrapper.artist.name

        if (wrapper.artist.genres.isNullOrEmpty()) {
            subtitleTextView.text = ""
            subtitleTextView.visibility = View.GONE
        } else {
            subtitleTextView.text = wrapper.artist.genres[0]
            subtitleTextView.visibility = View.VISIBLE
        }

        if (wrapper.artist.images.size > 0) {
            ImageLoader.get()
                .load(wrapper.artist.images[0].url)
                .into(imageView)
        }
    }
}