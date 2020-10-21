package com.pghaz.revery.spotify.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.CallSuper
import com.pghaz.revery.R
import com.pghaz.revery.alarm.adapter.base.BaseViewHolder
import com.pghaz.revery.alarm.model.BaseModel

abstract class BaseSpotifyViewHolder(view: View) : BaseViewHolder(view) {

    var onSpotifyItemClickListener: OnSpotifyItemClickListener? = null

    protected val titleTextView: TextView = view.findViewById(R.id.titleTextView)
    protected val subtitleTextView: TextView = view.findViewById(R.id.subtitleTextView)
    protected val imageView: ImageView = view.findViewById(R.id.imageView)

    @CallSuper
    override fun bind(model: BaseModel) {
        itemView.setOnClickListener {
            onSpotifyItemClickListener?.onClick(model)
        }
    }

    @CallSuper
    override fun onViewHolderRecycled() {
        itemView.setOnClickListener(null)
    }
}