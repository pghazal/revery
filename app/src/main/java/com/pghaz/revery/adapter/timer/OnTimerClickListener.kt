package com.pghaz.revery.adapter.timer

import com.pghaz.revery.model.app.Timer

interface OnTimerClickListener {
    fun onTimerClicked(timer: Timer)
    fun onPlayPauseButtonClicked(timer: Timer)
    fun onResetButtonClicked(timer: Timer)
}