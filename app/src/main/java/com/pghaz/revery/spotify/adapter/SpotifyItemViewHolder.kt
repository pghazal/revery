package com.pghaz.revery.spotify.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pghaz.revery.R
import com.squareup.picasso.Picasso
import kaaes.spotify.webapi.android.models.Image
import kaaes.spotify.webapi.android.models.PlaylistSimple

class SpotifyItemViewHolder(
    view: View,
    private val onSpotifyItemClickListener: OnSpotifyItemClickListener
) : RecyclerView.ViewHolder(view) {

    private val titleTextView: TextView = view.findViewById(R.id.titleTextView)
    private val subtitleTextView: TextView = view.findViewById(R.id.subtitleTextView)
    private val imageView: ImageView = view.findViewById(R.id.imageView)

    fun bind(item: PlaylistSimple) {
        itemView.setOnClickListener {
            onSpotifyItemClickListener.onClick(item)
        }

        titleTextView.text = item.name
        subtitleTextView.text = String.format("by %s", item.owner.display_name)

        if (item.images.size > 0) {
            val image: Image = item.images[0]
            Picasso.get()
                .load(image.url)
                .into(imageView)
        }
    }
}