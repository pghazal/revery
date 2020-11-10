package com.pghaz.revery.viewmodel.alarm

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.pghaz.revery.alarm.AlarmHandler
import com.pghaz.revery.model.app.Alarm
import com.pghaz.revery.repository.AlarmRepository
import com.pghaz.revery.viewmodel.BaseCreateEditViewModel

class CreateEditAlarmViewModel(application: Application) : BaseCreateEditViewModel(application) {

    private val alarmRepository = AlarmRepository(application)

    val timeChangedAlarmLiveData = MutableLiveData<Alarm>()

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