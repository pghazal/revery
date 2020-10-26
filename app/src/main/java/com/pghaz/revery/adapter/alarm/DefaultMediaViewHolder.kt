package com.pghaz.revery.adapter.alarm

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.CallSuper
import com.pghaz.revery.R
import com.pghaz.revery.adapter.base.BaseViewHolder
import com.pghaz.revery.model.app.alarm.AlarmMetadata
import com.pghaz.revery.model.app.BaseModel
import com.pghaz.revery.image.ImageLoader

class DefaultMediaViewHolder(view: View) : BaseViewHolder(view) {

    private val titleTextView: TextView = view.findViewById(R.id.titleTextView)
    private val subtitleTextView: TextView = view.findViewById(R.id.subtitleTextView)
    private val imageView: ImageView = view.findViewById(R.id.imageView)
    private val staticTextView: TextView = view.findViewById(R.id.staticTextView)

    @CallSuper
    override fun bind(model: BaseModel) {
        val metadata = model as AlarmMetadata

        bind(metadata.name, metadata.description, metadata.imageUrl)
    }

    @CallSuper
    override fun onViewHolderRecycled() {
        // do nothing
    }

    private fun bind(title: String?, subtitle: String?, imageUrl: String?) {
        titleTextView.text = title

        staticTextView.text = ""
        staticTextView.visibility = View.GONE

        if (subtitle.isNullOrEmpty()) {
            subtitleTextView.text = ""
            subtitleTextView.visibility = View.GONE
        } else {
            subtitleTextView.text = subtitle
            subtitleTextView.visibility = View.VISIBLE
        }

        ImageLoader.get()
            .placeholder(R.drawable.shape_alarm_ringtone_phone)
            .load(imageUrl)
            .into(imageView)
    }
}