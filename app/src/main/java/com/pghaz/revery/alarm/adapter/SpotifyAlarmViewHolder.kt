package com.pghaz.revery.alarm.adapter

import android.view.View
import com.pghaz.revery.R
import com.pghaz.revery.alarm.model.BaseModel
import com.pghaz.revery.alarm.model.app.SpotifyAlarm
import com.pghaz.revery.image.ImageLoader

class SpotifyAlarmViewHolder(view: View) : AlarmViewHolder(view) {

    override fun bind(model: BaseModel) {
        super.bind(model)

        val spotifyAlarm = model as SpotifyAlarm

        ImageLoader.get().load(spotifyAlarm.imageUrl)
            .placeholder(R.drawable.selector_alarm_image_background_color)
            .into(imageView)
    }
}