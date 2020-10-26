package com.pghaz.revery.adapter.spotify

import android.view.View
import com.pghaz.revery.R
import com.pghaz.revery.model.app.alarm.AlarmMetadata
import com.pghaz.revery.model.app.BaseModel
import com.pghaz.revery.model.app.spotify.PlaylistWrapper
import com.pghaz.revery.image.ImageLoader
import java.util.*

class SpotifyPlaylistViewHolder(view: View) : BaseSpotifyViewHolder(view) {

    override fun bind(model: BaseModel) {
        super.bind(model)

        when (model) {
            is PlaylistWrapper -> {
                bindPlaylistWrapper(model)
            }

            is AlarmMetadata -> {
                bindAlarmMetadata(model)
            }
        }
    }

    private fun bindPlaylistWrapper(model: PlaylistWrapper) {
        val title = model.playlistSimple.name
        val subtitle = model.playlistSimple.description
        val imageUrl = if (model.playlistSimple.images.size > 0) {
            model.playlistSimple.images[0].url
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
            subtitleTextView.context.getString(R.string.playlist)
        )

        ImageLoader.get()
            .placeholder(R.drawable.placeholder_square)
            .load(imageUrl)
            .into(imageView)
    }
}