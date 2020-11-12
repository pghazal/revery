package com.pghaz.revery.adapter.timer

import android.net.Uri
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import com.pghaz.revery.R
import com.pghaz.revery.adapter.base.BaseViewHolder
import com.pghaz.revery.extension.logError
import com.pghaz.revery.image.ImageLoader
import com.pghaz.revery.image.ImageUtils
import com.pghaz.revery.model.app.BaseModel
import com.pghaz.revery.model.app.Timer
import com.pghaz.revery.model.app.TimerState
import com.pghaz.revery.timer.TimerHandler

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
    private val playPauseButton: AppCompatImageButton = view.findViewById(R.id.playPauseButton)
    private val resetButton: AppCompatImageButton = view.findViewById(R.id.resetButton)

    private lateinit var timer: Timer

    private val mHandler: Handler = Handler()
    private val updateRemainingTimeRunnable = object : Runnable {
        override fun run() {
            val elapsedTime = TimerHandler.getElapsedTime(timer)
            updateRemainingTime(timer, elapsedTime)
            updatePlayPauseButton(timer)

            if (timer.state == TimerState.RUNNING) {
                mHandler.postDelayed(this, 1000L)
            }
        }
    }

    fun startUpdateTimer() {
        mHandler.postDelayed(updateRemainingTimeRunnable, 1000L)
    }

    fun stopUpdateTimer() {
        mHandler.removeCallbacks(updateRemainingTimeRunnable)
    }

    override fun bind(model: BaseModel) {
        timer = Timer(model as Timer)

        itemView.setOnClickListener {
            timerClickListener?.onTimerClicked(timer)
        }

        val elapsedTime = TimerHandler.getElapsedTime(timer)
        updateRemainingTime(timer, elapsedTime)
        updatePlayPauseButton(timer)

        if (TextUtils.isEmpty(timer.label)) {
            labelTextView.visibility = View.GONE
        } else {
            labelTextView.visibility = View.VISIBLE
        }

        labelTextView.text = timer.label

        playPauseButton.setOnClickListener {
            timerClickListener?.onPlayPauseButtonClicked(timer)
        }

        resetButton.setOnClickListener {
            timerClickListener?.onResetButtonClicked(timer)
        }

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

    private fun updatePlayPauseButton(timer: Timer) {
        when (timer.state) {
            TimerState.CREATED -> {
                playPauseButton.setImageResource(R.drawable.ic_play_filled)
            }

            TimerState.RUNNING -> {
                playPauseButton.setImageResource(R.drawable.ic_pause_filled)
            }

            TimerState.PAUSED -> {
                playPauseButton.setImageResource(R.drawable.ic_play_filled)
            }
        }
    }

    override fun onViewHolderRecycled() {
        itemView.setOnClickListener(null)
        playPauseButton.setOnClickListener(null)
        resetButton.setOnClickListener(null)
    }

    private fun updateRemainingTime(timer: Timer, elapsedTime: Long) {
        val milliseconds = if (elapsedTime > 0) {
            elapsedTime
        } else {
            timer.duration
        }

        hourDurationTextView.context.logError("milliseconds: $milliseconds")

        val seconds = (milliseconds / 1000).toInt() % 60
        val minutes = (milliseconds / (1000 * 60) % 60).toInt()
        val hours = (milliseconds / (1000 * 60 * 60) % 24).toInt()

        hourDurationTextView.context.logError("hours: $hours")
        hourDurationTextView.context.logError("minutes: $minutes")
        hourDurationTextView.context.logError("seconds: $seconds")

        hourDurationTextView.text = String.format("%02d", hours)
        minuteDurationTextView.text = String.format("%02d", minutes)
        secondDurationTextView.text = String.format("%02d", seconds)

        if (hours > 0) {
            hourDurationTextView.visibility = View.VISIBLE
            hourLabelTextView.visibility = View.VISIBLE
        } else {
            hourDurationTextView.visibility = View.GONE
            hourLabelTextView.visibility = View.GONE
        }

        minuteDurationTextView.visibility = View.VISIBLE
        minuteLabelTextView.visibility = View.VISIBLE

        if (seconds > 0) {
            secondDurationTextView.visibility = View.VISIBLE
            secondLabelTextView.visibility = View.VISIBLE
        } else {
            secondDurationTextView.visibility = View.GONE
            secondLabelTextView.visibility = View.GONE
        }

        if (hours == 0 && seconds == 0) {
            secondDurationTextView.visibility = View.VISIBLE
            secondLabelTextView.visibility = View.VISIBLE
        }
    }
}
