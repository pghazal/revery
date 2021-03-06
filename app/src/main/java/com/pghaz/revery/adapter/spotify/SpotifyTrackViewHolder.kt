package com.pghaz.revery.adapter.spotify

import android.view.View
import com.pghaz.revery.R
import com.pghaz.revery.image.ImageLoader
import com.pghaz.revery.model.app.BaseModel
import com.pghaz.revery.model.app.MediaMetadata
import com.pghaz.revery.model.app.spotify.ArtistWrapper
import com.pghaz.revery.model.app.spotify.TrackWrapper
import java.util.*

class SpotifyTrackViewHolder(view: View) : BaseSpotifyViewHolder(view) {

    override fun bind(model: BaseModel) {
        super.bind(model)

        when (model) {
            is TrackWrapper -> {
                bindTrackWrapper(model)
            }

            is MediaMetadata -> {
                bindAlarmMetadata(model)
            }
        }
    }

    private fun bindTrackWrapper(model: TrackWrapper) {
        val title = model.track.name
        val subtitle = ArtistWrapper.getArtistNames(model.track.artists)
        val imageUrl = if (model.track.album.images.size > 0) {
            model.track.album.images[0].url
        } else {
            null
        }

        if (!model.track.uri.isNullOrEmpty() && model.track.external_urls != null &&
            !model.track.external_urls["spotify"].isNullOrEmpty()
        ) {
            moreButton.visibility = View.VISIBLE
            moreButton.setOnClickListener {
                showMoreMenu(model.track.uri, model.track.external_urls["spotify"]!!)
            }
        } else {
            moreButton.visibility = View.GONE
        }

        bind(title, subtitle, imageUrl)
    }

    override fun bind(title: String?, subtitle: String?, imageUrl: String?) {
        titleTextView.text = title

        val format: String
        if (subtitle.isNullOrEmpty()) {
            subtitleTextView.text = ""
            subtitleTextView.visibility = View.GONE
            format = "%s"
        } else {
            subtitleTextView.text = subtitle
            subtitleTextView.visibility = View.VISIBLE
            format = "%s • "
        }

        staticTextView.visibility = View.VISIBLE
        staticTextView.text = String.format(
            Locale.getDefault(),
            format,
            subtitleTextView.context.getString(R.string.track)
        )

        ImageLoader.get()
            .placeholder(R.drawable.placeholder_square)
            .load(imageUrl)
            .into(imageView)
    }
}