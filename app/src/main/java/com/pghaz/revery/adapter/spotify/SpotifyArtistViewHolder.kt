package com.pghaz.revery.adapter.spotify

import android.view.View
import com.pghaz.revery.R
import com.pghaz.revery.image.ImageLoader
import com.pghaz.revery.model.app.BaseModel
import com.pghaz.revery.model.app.AlarmMetadata
import com.pghaz.revery.model.app.spotify.ArtistWrapper

class SpotifyArtistViewHolder(view: View) : BaseSpotifyViewHolder(view) {

    override fun bind(model: BaseModel) {
        super.bind(model)

        when (model) {
            is ArtistWrapper -> {
                bindArtistWrapper(model)
            }

            is AlarmMetadata -> {
                bindAlarmMetadata(model)
            }
        }
    }

    private fun bindArtistWrapper(model: ArtistWrapper) {
        val title = model.artist.name
        val subtitle = null
        val imageUrl = if (model.artist.images.size > 0) {
            model.artist.images[0].url
        } else {
            null
        }

        bind(title, subtitle, imageUrl)
    }

    override fun bind(title: String?, subtitle: String?, imageUrl: String?) {
        titleTextView.text = title

        subtitleTextView.visibility = View.VISIBLE
        subtitleTextView.text = subtitleTextView.context.getString(R.string.artist)

        staticTextView.visibility = View.GONE
        staticTextView.text = ""

        imageView.clipToOutline = true
        ImageLoader.get()
            .placeholder(R.drawable.placeholder_circle)
            .load(imageUrl)
            .into(imageView)
    }
}