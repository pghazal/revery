package com.pghaz.revery.spotify.adapter

import android.view.View
import com.pghaz.revery.alarm.model.BaseModel
import com.pghaz.revery.image.ImageLoader
import com.pghaz.revery.spotify.model.PlaylistWrapper

class SpotifyPlaylistViewHolder(view: View) : BaseSpotifyViewHolder(view) {

    override fun bind(model: BaseModel) {
        super.bind(model)

        val wrapper = model as PlaylistWrapper
        titleTextView.text = wrapper.playlistSimple.name

        if (wrapper.playlistSimple.description.isNullOrEmpty()) {
            subtitleTextView.text = ""
            subtitleTextView.visibility = View.GONE
        } else {
            subtitleTextView.text = wrapper.playlistSimple.description
            subtitleTextView.visibility = View.VISIBLE
        }

        if (wrapper.playlistSimple.images.size > 0) {
            ImageLoader.get()
                .load(wrapper.playlistSimple.images[0].url)
                .into(imageView)
        }
    }
}