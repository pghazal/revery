package com.pghaz.revery.adapter.timer

import android.net.Uri
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.pghaz.revery.R
import com.pghaz.revery.adapter.base.BaseViewHolder
import com.pghaz.revery.image.ImageLoader
import com.pghaz.revery.image.ImageUtils
import com.pghaz.revery.model.app.BaseModel
import com.pghaz.revery.model.app.Timer

open class TimerViewHolder(view: View) : BaseViewHolder(view) {
    var timerClickListener: OnTimerClickListener? = null

    private val hourDurationTextView: TextView = view.findViewById(R.id.hourDurationTextView)
    private val minuteDurationTextView: TextView = view.findViewById(R.id.minuteDurationTextView)
    private val secondDurationTextView: TextView = view.findViewById(R.id.secondDurationTextView)

    private val hourLabelTextView: TextView = view.findViewById(R.id.hourLabelTextView)
    private val minuteLabelTextView: TextView = view.findViewById(R.id.minuteLabelTextView)
    private val secondLabelTextView: TextView = view.findViewById(R.id.secondLabelTextView)

    private val labelTextView: TextView = view.findViewById(R.id.labelTextView)
    private val imageView: ImageView = view.findViewById(R.id.imageView)

    private val enableSwitch: SwitchCompat = view.findViewById(R.id.enableSwitch)

    private fun setDurationText(timer: Timer) {
        hourDurationTextView.text = String.format("%02d", timer.hour)
        minuteDurationTextView.text = String.format("%02d", timer.minute)
        secondDurationTextView.text = String.format("%02d", timer.second)

        if (timer.hour > 0) {
            hourDurationTextView.visibility = View.VISIBLE
            hourLabelTextView.visibility = View.VISIBLE
        } else {
            hourDurationTextView.visibility = View.GONE
            hourLabelTextView.visibility = View.GONE
        }

        minuteDurationTextView.visibility = View.VISIBLE
        minuteLabelTextView.visibility = View.VISIBLE

        if (timer.second > 0) {
            secondDurationTextView.visibility = View.VISIBLE
            secondLabelTextView.visibility = View.VISIBLE
        } else {
            secondDurationTextView.visibility = View.GONE
            secondLabelTextView.visibility = View.GONE
        }

        if (timer.hour == 0 && timer.second == 0) {
            secondDurationTextView.visibility = View.VISIBLE
            secondLabelTextView.visibility = View.VISIBLE
        }
    }

    override fun bind(model: BaseModel) {
        val timer = model as Timer

        itemView.setOnClickListener {
            timerClickListener?.onClick(Timer(timer))
        }

        setDurationText(timer)

        if (TextUtils.isEmpty(timer.label)) {
            labelTextView.visibility = View.GONE
        } else {
            labelTextView.visibility = View.VISIBLE
        }

        labelTextView.text = timer.label

        enableSwitch.isChecked = timer.enabled
        enableSwitch.setOnCheckedChangeListener { _, _ ->
            timerClickListener?.onToggle(Timer(timer))
        }

        imageView.isEnabled = timer.enabled
        hourDurationTextView.isEnabled = timer.enabled
        minuteDurationTextView.isEnabled = timer.enabled
        secondDurationTextView.isEnabled = timer.enabled
        labelTextView.isEnabled = timer.enabled

        val imageUri = Uri.parse(timer.metadata.imageUrl)
        val imageUrl = if (ImageUtils.isInternalFile(imageUri)) {
            if (ImageUtils.isCoverArtExists(imageUri)) {
                timer.metadata.imageUrl
            } else {
                ImageUtils.getCoverArtFilePath(imageView.context, Uri.parse(timer.metadata.uri))
            }
        } else {
            timer.metadata.imageUrl
        }

        ImageLoader.get().load(imageUrl)
            .placeholder(R.drawable.selector_alarm_image_background_color)
            .into(imageView)
    }

    override fun onViewHolderRecycled() {
        itemView.setOnClickListener(null)
        enableSwitch.setOnCheckedChangeListener(null)
    }
}
