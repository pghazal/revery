package com.pghaz.revery.spotify.adapter

import android.view.View
import android.widget.TextView
import com.pghaz.revery.R
import com.pghaz.revery.alarm.model.BaseModel
import com.pghaz.revery.image.ImageLoader
import com.pghaz.revery.spotify.model.TrackWrapper
import io.github.kaaes.spotify.webapi.core.models.ArtistSimple
import java.util.*

class SpotifyTrackViewHolder(view: View) : BaseSpotifyViewHolder(view) {

    private val staticTextView: TextView = view.findViewById(R.id.staticTextView)

    override fun bind(model: BaseModel) {
        super.bind(model)

        val wrapper = model as TrackWrapper
        titleTextView.text = wrapper.track.name

        staticTextView.text = String.format(
            Locale.getDefault(),
            "%s • ",
            staticTextView.context.getString(R.string.track)
        )

        val artistNames = getArtistNames(wrapper.track.artists)
        if (artistNames.isEmpty()) {
            subtitleTextView.text = ""
            subtitleTextView.visibility = View.GONE
        } else {
            subtitleTextView.text = artistNames
            subtitleTextView.visibility = View.VISIBLE
        }

        if (wrapper.track.album.images.size > 0) {
            ImageLoader.get()
                .load(wrapper.track.album.images[0].url)
                .into(imageView)
        }
    }

    private fun getArtistNames(artists: MutableList<ArtistSimple>): String {
        val stringBuilder = StringBuilder("")
        val iterator = artists.iterator()
        val artist = iterator.next()
        stringBuilder.append(artist.name)
        while (iterator.hasNext()) {
            val next = iterator.next()
            stringBuilder.append(", ")
            stringBuilder.append(next.name)
        }
        return stringBuilder.toString()
    }
}