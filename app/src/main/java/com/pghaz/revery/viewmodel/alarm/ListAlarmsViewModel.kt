package com.pghaz.revery.viewmodel.alarm

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.pghaz.revery.alarm.AlarmHandler
import com.pghaz.revery.model.app.alarm.Alarm
import com.pghaz.revery.repository.AlarmRepository

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