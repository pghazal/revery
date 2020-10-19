package com.pghaz.revery.alarm.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.pghaz.revery.alarm.AlarmHandler
import com.pghaz.revery.alarm.model.app.Alarm
import com.pghaz.revery.alarm.repository.AlarmRepository

class CreateEditAlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val alarmRepository = AlarmRepository(application)

    val timeChangedAlarmLiveData = MutableLiveData<Alarm>()
    val alarmMetadataLiveData = MutableLiveData<Alarm>()

    fun createAlarm(context: Context?, alarm: Alarm) {
        alarmRepository.insert(alarm)
        AlarmHandler.scheduleAlarm(context, alarm)
    }

    fun editAlarm(context: Context?, alarm: Alarm) {
        AlarmHandler.cancelAlarm(context, alarm)
        AlarmHandler.scheduleAlarm(context, alarm)

        alarmRepository.update(alarm)
    }

    fun delete(context: Context?, alarm: Alarm) {
        AlarmHandler.cancelAlarm(context, alarm)
        alarmRepository.delete(alarm)
    }
}