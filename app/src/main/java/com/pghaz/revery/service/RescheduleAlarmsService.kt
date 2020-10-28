package com.pghaz.revery.service

import android.app.Notification
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.pghaz.revery.R
import com.pghaz.revery.alarm.AlarmHandler
import com.pghaz.revery.application.ReveryApplication
import com.pghaz.revery.repository.AlarmRepository

class RescheduleAlarmsService : LifecycleService() {

    private lateinit var alarmRepository: AlarmRepository

    override fun onCreate() {
        super.onCreate()
        alarmRepository = AlarmRepository(application)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        startForeground(2, buildRescheduleNotification())

        alarmRepository.getAlarmsLiveData().observe(this, { alarms ->
            alarms.forEach {
                if (it.enabled) {
                    AlarmHandler.cancelAlarm(this, it)
                    AlarmHandler.scheduleAlarm(this, it)
                }
            }

            stopSelf()
        })

        return START_STICKY
    }

    private fun buildRescheduleNotification(): Notification {
        return NotificationCompat.Builder(this, ReveryApplication.CHANNEL_ID)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.rescheduling_in_progress))
            .setSmallIcon(R.drawable.ic_revery_transparent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(ContextCompat.getColor(this, R.color.colorAccent))
            .setAutoCancel(false)
            .setProgress(100, 0, true)
            .build()
    }
}