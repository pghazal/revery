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
        val wrapper = model as PlaylistWrapper

        itemView.setOnClickListener {
            onSpotifyItemClickListener?.onClick(wrapper)
        }

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

    override fun onViewHolderRecycled() {
        // do nothing
    }
}