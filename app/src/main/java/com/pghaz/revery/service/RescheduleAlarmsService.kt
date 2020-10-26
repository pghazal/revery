package com.pghaz.revery.service

import android.content.Intent
import androidx.lifecycle.LifecycleService
import com.pghaz.revery.alarm.AlarmHandler
import com.pghaz.revery.repository.AlarmRepository

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
                    AlarmHandler.cancelAlarm(this, it)
                    AlarmHandler.scheduleAlarm(this, it)
                }
            }

            stopSelf()
        })

        return START_NOT_STICKY
    }
}