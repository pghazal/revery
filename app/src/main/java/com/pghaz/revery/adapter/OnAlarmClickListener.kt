package com.pghaz.revery.adapter

import com.pghaz.revery.repository.Alarm

interface OnAlarmClickListener {
    fun onClick(alarm: Alarm)
    fun onToggle(alarm: Alarm)
}