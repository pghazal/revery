package com.pghaz.revery.alarm.service

import android.content.Intent
import androidx.lifecycle.LifecycleService
import com.pghaz.revery.alarm.AlarmHandler
import com.pghaz.revery.alarm.repository.AlarmRepository

class RescheduleAlarmsService : LifecycleService() {

    private lateinit var alarmRepository: AlarmRepository

    override fun onCreate() {
        super.onCreate()
        alarmRepository = AlarmRepository(application)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        alarmRepository.getAlarmsLiveData().observe(this, { alarms ->
            alarms.forEach {
                if (it.enabled) {
                    AlarmHandler.scheduleAlarm(this, it)
                }
            }
        })

        return START_STICKY
    }
}