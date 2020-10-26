package com.pghaz.revery.adapter.spotify

import android.view.View
import com.pghaz.revery.R
import com.pghaz.revery.model.app.alarm.AlarmMetadata
import com.pghaz.revery.model.app.BaseModel
import com.pghaz.revery.model.app.spotify.AlbumWrapper
import com.pghaz.revery.model.app.spotify.ArtistWrapper
import com.pghaz.revery.image.ImageLoader
import java.util.*

class SpotifyAlbumViewHolder(view: View) : BaseSpotifyViewHolder(view) {

    override fun bind(model: BaseModel) {
        super.bind(model)

        when (model) {
            is AlbumWrapper -> {
                bindAlbumWrapper(model)
            }

            is AlarmMetadata -> {
                bindAlarmMetadata(model)
            }
        }
    }

    private fun bindAlbumWrapper(model: AlbumWrapper) {
        val title = model.album.name
        val subtitle = ArtistWrapper.getArtistNames(model.album.artists)
        val imageUrl = if (model.album.images.size > 0) {
            model.album.images[0].url
        } else {
            null
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
            subtitleTextView.context.getString(R.string.album)
        )

        ImageLoader.get().load(imageUrl).into(imageView)
    }
}