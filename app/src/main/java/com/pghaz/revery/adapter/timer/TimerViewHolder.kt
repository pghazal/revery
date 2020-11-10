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
import java.util.*

open class TimerViewHolder(view: View) : BaseViewHolder(view) {
    var timerClickListener: OnTimerClickListener? = null

    private val timeTextView: TextView = view.findViewById(R.id.timeTextView)
    private val amPmTextView: TextView = view.findViewById(R.id.amPmTextView)
    private val labelTextView: TextView = view.findViewById(R.id.labelTextView)
    private val timeRemainingTextView: TextView = view.findViewById(R.id.timeRemainingTextView)
    private val imageView: ImageView = view.findViewById(R.id.imageView)

    private val enableSwitch: SwitchCompat = view.findViewById(R.id.enableSwitch)

    private fun setTimeText(timer: Timer) {
        timeTextView.text = String.format(
            Locale.getDefault(), "%d",
            timer.durationInSeconds
        )
    }

    override fun bind(model: BaseModel) {
        val timer = model as Timer

        itemView.setOnClickListener {
            timerClickListener?.onClick(Timer(timer))
        }

        setTimeText(timer)

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
        timeTextView.isEnabled = timer.enabled
        amPmTextView.isEnabled = timer.enabled
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

        if (timer.enabled) {
            timeRemainingTextView.visibility = View.VISIBLE
            /*val timeRemainingInfo = DateTimeUtils.getTimeRemaining(timer)
            timeRemainingTextView.text =
                DateTimeUtils.getRemainingTimeText(timeRemainingTextView.context, timeRemainingInfo)*/
        } else {
            timeRemainingTextView.visibility = View.GONE
            timeRemainingTextView.text = ""
        }
    }

    override fun onViewHolderRecycled() {
        itemView.setOnClickListener(null)
        enableSwitch.setOnCheckedChangeListener(null)
    }
}
