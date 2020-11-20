package com.pghaz.revery.adapter.timer

import android.animation.ValueAnimator
import android.net.Uri
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import com.pghaz.revery.R
import com.pghaz.revery.adapter.base.BaseViewHolder
import com.pghaz.revery.image.ImageLoader
import com.pghaz.revery.image.ImageUtils
import com.pghaz.revery.model.app.BaseModel
import com.pghaz.revery.model.app.MediaType
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
    private val incrementButton: AppCompatButton = view.findViewById(R.id.incrementButton)
    private val circularProgressBar: ProgressBar = view.findViewById(R.id.circularProgressBar)

    private var progressAnimator = ValueAnimator()

    private lateinit var timer: Timer

    private val mHandler: Handler = Handler()
    private val updateRemainingTimeRunnable = object : Runnable {
        override fun run() {
            val remainingTime = TimerHandler.getRemainingTime(timer)
            updateRemainingTime(timer, remainingTime)
            updatePlayPauseButton(timer)

            if (timer.state == TimerState.RUNNING ||
                timer.state == TimerState.RINGING
            ) {
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
            timerClickListener?.onTimerClicked(Timer(timer))
        }

        val elapsedTime = TimerHandler.getElapsedTime(timer)
        val remainingTime = TimerHandler.getRemainingTime(timer)
        updateProgress(timer, elapsedTime, remainingTime)
        updateRemainingTime(timer, remainingTime)
        updatePlayPauseButton(timer)

        if (TextUtils.isEmpty(timer.label)) {
            labelTextView.visibility = View.GONE
        } else {
            labelTextView.visibility = View.VISIBLE
        }

        labelTextView.text = timer.label

        playPauseButton.setOnClickListener {
            timerClickListener?.onPlayPauseButtonClicked(Timer(timer))
        }

        resetButton.setOnClickListener {
            timerClickListener?.onResetButtonClicked(Timer(timer))
        }

        incrementButton.setOnClickListener {
            timerClickListener?.onIncrementButtonClicked(Timer(timer))
        }

        val imageUrl = if (timer.metadata.type != MediaType.NONE) {
            val imageUri = Uri.parse(timer.metadata.imageUrl)
            if (ImageUtils.isInternalFile(imageUri)) {
                if (ImageUtils.isCoverArtExists(imageUri)) {
                    timer.metadata.imageUrl
                } else {
                    ImageUtils.getCoverArtFilePath(imageView.context, Uri.parse(timer.metadata.uri))
                }
            } else {
                timer.metadata.imageUrl
            }
        } else {
            null
        }

        ImageLoader.get().load(imageUrl)
            .placeholder(R.drawable.selector_alarm_image_background_color)
            .into(imageView)
    }

    private fun updatePlayPauseButton(timer: Timer) {
        when (timer.state) {
            TimerState.CREATED -> {
                playPauseButton.setImageResource(R.drawable.ic_play_filled)
                resetButton.visibility = View.INVISIBLE
                incrementButton.visibility = View.INVISIBLE
                circularProgressBar.visibility = View.GONE
            }

            TimerState.RUNNING -> {
                playPauseButton.setImageResource(R.drawable.ic_pause_filled)
                resetButton.visibility = View.INVISIBLE
                incrementButton.visibility = View.VISIBLE
                circularProgressBar.visibility = View.VISIBLE
            }

            TimerState.RINGING -> {
                playPauseButton.setImageResource(R.drawable.ic_stop_filled)
                resetButton.visibility = View.INVISIBLE
                incrementButton.visibility = View.VISIBLE
                circularProgressBar.visibility = View.VISIBLE
            }

            TimerState.PAUSED -> {
                playPauseButton.setImageResource(R.drawable.ic_play_filled)
                resetButton.visibility = View.VISIBLE
                incrementButton.visibility = View.INVISIBLE
                circularProgressBar.visibility = View.VISIBLE
            }
        }
    }

    override fun onViewHolderRecycled() {
        itemView.setOnClickListener(null)
        playPauseButton.setOnClickListener(null)
        resetButton.setOnClickListener(null)
        incrementButton.setOnClickListener(null)
        progressAnimator.cancel()
        stopUpdateTimer()
    }

    /**
     * Copyright Mahmoud Bentriou <3
     */
    /*val N = 100
    private fun l(timer: Timer, i: Int): Long {
        return i * ((timer.duration + timer.extraTime) / N)
    }

    private fun getProgressWithMoud(timer: Timer, elapsedTime: Long): Int {
        var progress = 0
        for (k in 0..N) {
            if (l(timer, k) <= elapsedTime && l(timer, k + 1) > elapsedTime) {
                progress = k
                break
            }
        }
        return progress
    }*/

    private fun updateProgress(timer: Timer, elapsedTime: Long, remainingTime: Long) {
        val remainingMilliseconds = if (remainingTime > 0) {
            remainingTime
        } else {
            timer.duration
        }

        val progress = if (timer.state == TimerState.RINGING) {
            100
        } else {
            val step = (timer.duration + timer.extraTime) / 100
            (elapsedTime / step).toInt()
        }

        circularProgressBar.progress = progress

        /*circularProgressBar.context.logError("remainingMilliseconds: $remainingMilliseconds")
        circularProgressBar.context.logError("progress: $progress")*/

        progressAnimator.duration = remainingMilliseconds
        progressAnimator.setIntValues(progress, 100)
        progressAnimator.removeAllUpdateListeners()
        progressAnimator.addUpdateListener { animation ->
            circularProgressBar.progress = animation.animatedValue as Int
        }

        when (timer.state) {
            TimerState.RUNNING -> {
                if (!progressAnimator.isStarted) {
                    progressAnimator.start()
                } else if (progressAnimator.isPaused) {
                    progressAnimator.resume()
                }
            }

            TimerState.PAUSED -> {
                if (!progressAnimator.isPaused) {
                    progressAnimator.pause()
                }
            }

            else -> {
                // do nothing
            }
        }
    }

    private fun updateRemainingTime(timer: Timer, remainingTime: Long) {
        val milliseconds = if (remainingTime > 0) {
            remainingTime
        } else {
            timer.duration
        }

        val seconds = (milliseconds / 1000).toInt() % 60
        val minutes = (milliseconds / (1000 * 60) % 60).toInt()
        val hours = (milliseconds / (1000 * 60 * 60) % 24).toInt()

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
