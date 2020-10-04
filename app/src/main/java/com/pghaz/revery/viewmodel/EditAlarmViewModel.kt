package com.pghaz.revery.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.pghaz.revery.alarm.AlarmHandler
import com.pghaz.revery.repository.Alarm
import com.pghaz.revery.repository.AlarmRepository

class EditAlarmViewModel(application: Application) :
    AndroidViewModel(application) {

    private val alarmHandler = AlarmHandler()
    private val alarmRepository = AlarmRepository(application)

    fun edit(context: Context?, alarm: Alarm) {
        alarmHandler.cancelAlarm(context, alarm)
        alarmHandler.scheduleAlarm(context, alarm)

        alarmRepository.update(alarm)
    }

    fun delete(context: Context?, alarm: Alarm) {
        alarmHandler.cancelAlarm(context, alarm)
        alarmRepository.delete(alarm)
    }
}