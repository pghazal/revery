package com.pghaz.revery.alarm.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.pghaz.revery.alarm.AlarmHandler
import com.pghaz.revery.alarm.model.app.AbstractAlarm
import com.pghaz.revery.alarm.model.app.Alarm
import com.pghaz.revery.alarm.repository.AlarmRepository

class ListAlarmsViewModel(application: Application) : AndroidViewModel(application) {

    private val alarmRepository = AlarmRepository(application)
    val alarmsLiveData = alarmRepository.getAllAlarmsLiveData()

    fun scheduleAlarm(context: Context?, alarm: AbstractAlarm) {
        AlarmHandler.scheduleAlarm(context, alarm)
    }

    fun cancelAlarm(context: Context?, alarm: AbstractAlarm) {
        AlarmHandler.cancelAlarm(context, alarm)
    }

    fun update(alarm: AbstractAlarm) {
        alarmRepository.update(alarm)
    }

    fun delete(alarm: AbstractAlarm) {
        alarmRepository.delete(alarm)
    }
}