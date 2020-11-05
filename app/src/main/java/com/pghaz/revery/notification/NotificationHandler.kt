package com.pghaz.revery.notification

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat


object NotificationHandler {

    const val CHANNEL_ID = "REVERY_ALARM_SERVICE_CHANNEL"

    const val NOTIFICATION_ID_ALARM = 1
    const val NOTIFICATION_ID_RESCHEDULE = 2

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Revery Notification",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
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

    fun isNotificationEnabled(context: Context): Boolean {
        val notificationManager = NotificationManagerCompat.from(context.applicationContext)

        var notificationChannelEnabled = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(CHANNEL_ID)?.importance == NotificationManagerCompat.IMPORTANCE_NONE) {
                notificationChannelEnabled = false
            }
        }

        return notificationManager.areNotificationsEnabled() && notificationChannelEnabled
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

    fun startDoNotDisturbActivity(activity: Activity, requestCode: Int) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        activity.startActivityForResult(intent, requestCode)
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