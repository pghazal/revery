package com.pghaz.revery.alarm.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.pghaz.revery.alarm.AlarmHandler
import com.pghaz.revery.alarm.repository.Alarm
import com.pghaz.revery.alarm.repository.AlarmRepository

class ListAlarmsViewModel(application: Application) : AndroidViewModel(application) {

    private val alarmHandler = AlarmHandler()
    private val alarmRepository = AlarmRepository(application)
    private val alarmsLiveData = alarmRepository.getAlarmsLiveData()

    fun getAlarmsLiveData(): LiveData<List<Alarm>> {
        return alarmsLiveData
    }

    fun scheduleAlarm(context: Context?, alarm: Alarm) {
        alarmHandler.scheduleAlarm(context, alarm)
    }

    fun cancelAlarm(context: Context?, alarm: Alarm) {
        alarmHandler.cancelAlarm(context, alarm)
    }

    fun update(alarm: Alarm) {
        alarmRepository.update(alarm)
    }

    fun delete(alarm: Alarm) {
        alarmRepository.delete(alarm)
    }
}