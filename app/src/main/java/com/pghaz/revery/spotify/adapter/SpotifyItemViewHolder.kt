package com.pghaz.revery.spotify.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.pghaz.revery.R
import com.pghaz.revery.alarm.adapter.base.BaseViewHolder
import com.pghaz.revery.alarm.model.BaseModel
import com.pghaz.revery.image.ImageLoader
import com.pghaz.revery.spotify.model.PlaylistWrapper

class SpotifyItemViewHolder(view: View) : BaseViewHolder(view) {

    var onSpotifyItemClickListener: OnSpotifyItemClickListener? = null

    private val titleTextView: TextView = view.findViewById(R.id.titleTextView)
    private val subtitleTextView: TextView = view.findViewById(R.id.subtitleTextView)
    private val imageView: ImageView = view.findViewById(R.id.imageView)

    override fun bind(model: BaseModel) {
        val item = model as PlaylistWrapper

        itemView.setOnClickListener {
            onSpotifyItemClickListener?.onClick(item)
        }

        titleTextView.text = item.playlistSimple.name
        subtitleTextView.text = String.format("by %s", item.playlistSimple.owner.display_name)

        if (item.playlistSimple.images.size > 0) {
            ImageLoader.get()
                .load(item.playlistSimple.images[0].url)
                .into(imageView)
        }
    }

    override fun onViewHolderRecycled() {
        // do nothing
    }
}