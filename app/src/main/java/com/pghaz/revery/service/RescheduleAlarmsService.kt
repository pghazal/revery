package com.pghaz.revery.service

import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleService
import com.pghaz.revery.MainActivity
import com.pghaz.revery.R
import com.pghaz.revery.alarm.AlarmHandler
import com.pghaz.revery.application.ReveryApplication
import com.pghaz.revery.repository.AlarmRepository

class RescheduleAlarmsService : LifecycleService() {

    private lateinit var alarmRepository: AlarmRepository
    private var hasEnabledAlarms = false

    companion object {
        const val NOTIFICATION_ID = 2

        fun clearNotification(context: Context) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIFICATION_ID)
        }

        fun rescheduleEnabledAlarms(application: Application, lifecycleOwner: LifecycleOwner) {
            val alarmRepository = AlarmRepository(application)

            val liveData = alarmRepository.getAlarmsLiveData()
            liveData.observe(lifecycleOwner, { alarms ->
                alarms.forEach {
                    if (it.enabled) {
                        AlarmHandler.cancelAlarm(application, it)
                        AlarmHandler.scheduleAlarm(application, it)
                    }
                }
                liveData.removeObservers(lifecycleOwner)
            })
        }
    }

    override fun onCreate() {
        super.onCreate()
        alarmRepository = AlarmRepository(application)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        startForeground(NOTIFICATION_ID, buildRescheduleNotification())

        alarmRepository.getAlarmsLiveData().observe(this, { alarms ->
            alarms.forEach {
                if (it.enabled) {
                    hasEnabledAlarms = true
                    AlarmHandler.cancelAlarm(this, it)
                    AlarmHandler.scheduleAlarm(this, it)
                }
            }

            if (hasEnabledAlarms) {
                stopForeground(false)
            } else {
                stopForeground(true)
            }

            stopSelf()
        })

        return START_STICKY
    }

    private fun buildRescheduleNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

        val notificationRequestCode = NOTIFICATION_ID
        val notificationPendingIntent = PendingIntent.getActivity(
            this,
            notificationRequestCode,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, ReveryApplication.CHANNEL_ID)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentText(getString(R.string.rescheduling_alarms_after_reboot))
            .setContentIntent(notificationPendingIntent)
            .setSmallIcon(R.drawable.ic_revery_transparent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(ContextCompat.getColor(this, R.color.colorAccent))
            .setAutoCancel(true)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(getString(R.string.rescheduling_alarms_after_reboot))
            )
            .build()
    }
}