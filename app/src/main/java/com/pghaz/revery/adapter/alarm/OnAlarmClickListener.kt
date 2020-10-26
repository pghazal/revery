package com.pghaz.revery.adapter.alarm

import com.pghaz.revery.model.app.alarm.Alarm

interface OnAlarmClickListener {
    fun onClick(alarm: Alarm)
    fun onToggle(alarm: Alarm)
}