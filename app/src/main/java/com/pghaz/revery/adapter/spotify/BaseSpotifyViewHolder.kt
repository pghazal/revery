package com.pghaz.revery.adapter.spotify

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.CallSuper
import com.pghaz.revery.R
import com.pghaz.revery.adapter.base.BaseViewHolder
import com.pghaz.revery.model.app.alarm.AlarmMetadata
import com.pghaz.revery.model.app.BaseModel

abstract class BaseSpotifyViewHolder(view: View) : BaseViewHolder(view) {

    var onSpotifyItemClickListener: OnSpotifyItemClickListener? = null

    protected val titleTextView: TextView = view.findViewById(R.id.titleTextView)
    protected val subtitleTextView: TextView = view.findViewById(R.id.subtitleTextView)
    protected val imageView: ImageView = view.findViewById(R.id.imageView)
    protected val staticTextView: TextView = view.findViewById(R.id.staticTextView)

    @CallSuper
    override fun bind(model: BaseModel) {
        itemView.setOnClickListener {
            onSpotifyItemClickListener?.onClick(model)
        }
    }

    protected fun bindAlarmMetadata(metadata: AlarmMetadata) {
        bind(metadata.name, metadata.description, metadata.imageUrl)
    }

    abstract fun bind(title: String?, subtitle: String?, imageUrl: String?)

    @CallSuper
    override fun onViewHolderRecycled() {
        itemView.setOnClickListener(null)
    }
}