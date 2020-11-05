package com.pghaz.revery.service

import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleService
import com.pghaz.revery.MainActivity
import com.pghaz.revery.R
import com.pghaz.revery.alarm.AlarmHandler
import com.pghaz.revery.notification.NotificationHandler
import com.pghaz.revery.repository.AlarmRepository
import com.pghaz.revery.util.Arguments

class RescheduleAlarmsService : LifecycleService() {

    private lateinit var alarmRepository: AlarmRepository
    private var hasEnabledAlarms = false

    companion object {
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

        fun isRebootAction(action: String?): Boolean {
            if (Intent.ACTION_BOOT_COMPLETED == action ||
                "android.intent.action.QUICKBOOT_POWERON" == action ||
                "com.htc.intent.action.QUICKBOOT_POWERON" == action
            ) {
                return true
            }

            return false
        }
    }

    override fun onCreate() {
        super.onCreate()
        alarmRepository = AlarmRepository(application)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        startForeground(
            NotificationHandler.NOTIFICATION_ID_RESCHEDULE,
            buildRescheduleNotification()
        )

        alarmRepository.getAlarmsLiveData().observe(this, { alarms ->
            alarms.forEach {
                if (it.enabled) {
                    hasEnabledAlarms = true
                    AlarmHandler.cancelAlarm(this, it)
                    AlarmHandler.scheduleAlarm(this, it)
                }
            }

            val scheduledBy = intent?.getStringExtra(Arguments.ARGS_RESCHEDULED_BY_ACTION)
            if (isRebootAction(scheduledBy) && hasEnabledAlarms) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_DETACH)
                } else {
                    stopForeground(true)
                }
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

        val notificationRequestCode = NotificationHandler.NOTIFICATION_ID_RESCHEDULE
        val notificationPendingIntent = PendingIntent.getActivity(
            this,
            notificationRequestCode,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, NotificationHandler.CHANNEL_ID_ALARM_RESCHEDULE)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentText(getString(R.string.rescheduling_alarms_after_reboot))
            .setContentIntent(notificationPendingIntent)
            .setSmallIcon(R.drawable.ic_revery_transparent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setColor(ContextCompat.getColor(this, R.color.colorAccent))
            .setAutoCancel(true)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(getString(R.string.rescheduling_alarms_after_reboot))
            )
            .build()
    }
}