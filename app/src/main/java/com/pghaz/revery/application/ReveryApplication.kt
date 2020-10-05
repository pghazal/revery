package com.pghaz.revery.application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build


class ReveryApplication : Application() {

    companion object {
        const val CHANNEL_ID = "REVERY_ALARM_SERVICE_CHANNEL"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Revery Notification",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }
}