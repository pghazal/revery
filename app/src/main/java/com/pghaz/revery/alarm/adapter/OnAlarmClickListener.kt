package com.pghaz.revery.alarm.adapter

import com.pghaz.revery.alarm.model.app.AbstractAlarm

interface OnAlarmClickListener {
    fun onClick(alarm: AbstractAlarm)
    fun onToggle(alarm: AbstractAlarm)
}