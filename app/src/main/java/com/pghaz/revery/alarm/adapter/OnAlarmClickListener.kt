package com.pghaz.revery.alarm.adapter

import com.pghaz.revery.alarm.model.app.Alarm

interface OnAlarmClickListener {
    fun onClick(alarm: Alarm)
    fun onToggle(alarm: Alarm)
}