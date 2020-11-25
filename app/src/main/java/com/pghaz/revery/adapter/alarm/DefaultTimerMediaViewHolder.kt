package com.pghaz.revery.adapter.alarm

import android.view.View
import com.pghaz.revery.R
import com.pghaz.revery.image.ImageLoader

class DefaultTimerMediaViewHolder(view: View) : DefaultMediaViewHolder(view) {

    override fun bind(title: String?, subtitle: String?, imageUrl: String?) {
        super.bind(title, subtitle, imageUrl)

        ImageLoader.get()
            .placeholder(R.drawable.shape_timer_ringtone_phone)
            .load(imageUrl)
            .into(imageView)
    }
}