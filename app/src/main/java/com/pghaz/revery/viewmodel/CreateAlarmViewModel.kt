package com.pghaz.revery.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.pghaz.revery.alarm.AlarmHandler
import com.pghaz.revery.repository.Alarm
import com.pghaz.revery.repository.AlarmRepository


class CreateAlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val alarmHandler = AlarmHandler()
    private val alarmRepository = AlarmRepository(application)

    fun createAlarm(context: Context?, alarm: Alarm) {
        alarmRepository.insert(alarm)
        alarmHandler.scheduleAlarm(context, alarm)
    }
}