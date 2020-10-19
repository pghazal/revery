package com.pghaz.revery.alarm.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.pghaz.revery.alarm.AlarmHandler
import com.pghaz.revery.alarm.model.app.Alarm
import com.pghaz.revery.alarm.repository.AlarmRepository

class ListAlarmsViewModel(application: Application) : AndroidViewModel(application) {

    private val alarmRepository = AlarmRepository(application)
    val alarmsLiveData = alarmRepository.getAlarmsLiveData()

    fun scheduleAlarm(context: Context?, alarm: Alarm) {
        AlarmHandler.scheduleAlarm(context, alarm)
    }

    fun cancelAlarm(context: Context?, alarm: Alarm) {
        AlarmHandler.cancelAlarm(context, alarm)
    }

    fun update(alarm: Alarm) {
        alarmRepository.update(alarm)
    }

    fun delete(alarm: Alarm) {
        alarmRepository.delete(alarm)
    }
}