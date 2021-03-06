package com.pghaz.revery.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.pghaz.revery.LauncherForResultComponent
import com.pghaz.revery.R


object NotificationHandler {

    @Deprecated("This is an old channel")
    const val CHANNEL_ID_ALARM_DEPRECATED = "REVERY_ALARM_SERVICE_CHANNEL"

    const val CHANNEL_ID_ALARM = "REVERY_CHANNEL_ALARM"
    const val CHANNEL_ID_ALARM_RESCHEDULE = "REVERY_CHANNEL_ALARM_RESCHEDULE"
    const val CHANNEL_ID_ALARM_SNOOZE = "REVERY_CHANNEL_ALARM_SNOOZE"
    const val CHANNEL_ID_ALARM_ERROR = "REVERY_CHANNEL_ALARM_ERROR"
    const val CHANNEL_ID_TIMER = "REVERY_CHANNEL_TIMER"
    const val CHANNEL_ID_STANDBY = "REVERY_CHANNEL_STANDBY"

    const val NOTIFICATION_ID_ALARM = 1
    const val NOTIFICATION_ID_RESCHEDULE = 2
    const val NOTIFICATION_ID_ERROR_OCCURRED = 3
    const val NOTIFICATION_ID_TIMER_OVER = 4
    const val NOTIFICATION_ID_TIMER_RUNNING = 5
    const val NOTIFICATION_ID_STANDBY = 6

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            @Suppress("DEPRECATION")
            manager?.deleteNotificationChannel(CHANNEL_ID_ALARM_DEPRECATED)

            val alarmChannel = NotificationChannel(
                CHANNEL_ID_ALARM,
                context.getString(R.string.notification_channel_alarm),
                NotificationManager.IMPORTANCE_HIGH
            )
            alarmChannel.vibrationPattern = longArrayOf(0)
            alarmChannel.enableVibration(true)

            val alarmSnoozeChannel = NotificationChannel(
                CHANNEL_ID_ALARM_SNOOZE,
                context.getString(R.string.notification_channel_alarm_snooze),
                NotificationManager.IMPORTANCE_HIGH
            )
            alarmSnoozeChannel.vibrationPattern = longArrayOf(0)
            alarmSnoozeChannel.enableVibration(true)

            val alarmRescheduleChannel = NotificationChannel(
                CHANNEL_ID_ALARM_RESCHEDULE,
                context.getString(R.string.notification_channel_alarm_reschedule),
                NotificationManager.IMPORTANCE_HIGH
            )
            alarmRescheduleChannel.vibrationPattern = longArrayOf(0)
            alarmRescheduleChannel.enableVibration(true)

            val emergencyAlarmChannel = NotificationChannel(
                CHANNEL_ID_ALARM_ERROR,
                context.getString(R.string.notification_channel_alarm_error),
                NotificationManager.IMPORTANCE_HIGH
            )
            emergencyAlarmChannel.vibrationPattern = longArrayOf(0)
            emergencyAlarmChannel.enableVibration(true)

            val timerChannel = NotificationChannel(
                CHANNEL_ID_TIMER,
                context.getString(R.string.notification_channel_timer),
                NotificationManager.IMPORTANCE_HIGH
            )
            timerChannel.vibrationPattern = longArrayOf(0)
            timerChannel.enableVibration(true)

            val standbyChannel = NotificationChannel(
                CHANNEL_ID_STANDBY,
                context.getString(R.string.notification_channel_standby),
                NotificationManager.IMPORTANCE_HIGH
            )
            timerChannel.vibrationPattern = longArrayOf(0)
            timerChannel.enableVibration(true)

            manager?.createNotificationChannel(alarmChannel)
            manager?.createNotificationChannel(alarmSnoozeChannel)
            manager?.createNotificationChannel(alarmRescheduleChannel)
            manager?.createNotificationChannel(emergencyAlarmChannel)
            manager?.createNotificationChannel(timerChannel)
            manager?.createNotificationChannel(standbyChannel)
        }
    }

    fun notify(context: Context, id: Int, notification: Notification) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(id, notification)
    }

    fun cancel(context: Context, id: Int) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(id)
    }

    fun areNeededNotificationsEnabled(context: Context): Boolean {
        val notificationManager = NotificationManagerCompat.from(context.applicationContext)

        var notificationChannelsEnabled = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(CHANNEL_ID_ALARM)?.importance == NotificationManagerCompat.IMPORTANCE_NONE ||
                notificationManager.getNotificationChannel(CHANNEL_ID_ALARM_SNOOZE)?.importance == NotificationManagerCompat.IMPORTANCE_NONE
            ) {
                notificationChannelsEnabled = false
            }
        }

        return notificationManager.areNotificationsEnabled() && notificationChannelsEnabled
    }

    fun openAppNotificationSettings(context: Context) {
        val intent = Intent().apply {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                    action = "android.settings.APP_NOTIFICATION_SETTINGS"
                    putExtra("app_package", context.packageName)
                    putExtra("app_uid", context.applicationInfo.uid)
                }
                else -> {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    addCategory(Intent.CATEGORY_DEFAULT)
                    data = Uri.parse("package:" + context.packageName)
                }
            }
        }
        context.startActivity(intent)
    }

    fun isNotificationPolicyAccessGranted(notificationManager: NotificationManager): Boolean {
        return notificationManager.isNotificationPolicyAccessGranted
    }

    fun startDoNotDisturbActivity(
        context: Context,
        launcherForResultComponent: LauncherForResultComponent,
        requestCode: Int
    ) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        if (intent.resolveActivity(context.packageManager) != null) {
            launcherForResultComponent.launchActivityForResult(intent, requestCode)
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.do_not_disturb_cannot_handle),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun isDoNotDisturbEnabled(notificationManager: NotificationManager): Boolean {
        return notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_PRIORITY ||
                notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALARMS ||
                notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE
    }

    fun setInterruptionFilter(
        notificationManager: NotificationManager,
        interruptionFilter: Int
    ) {
        if (isNotificationPolicyAccessGranted(notificationManager)) {
            notificationManager.setInterruptionFilter(interruptionFilter)
        }
    }
}