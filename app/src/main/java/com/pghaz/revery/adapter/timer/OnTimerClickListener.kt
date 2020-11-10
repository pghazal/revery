package com.pghaz.revery.adapter.timer

import com.pghaz.revery.model.app.Timer

interface OnTimerClickListener {
    fun onClick(timer: Timer)
    fun onToggle(timer: Timer)
}