package com.pghaz.revery.application

import android.app.Application
import com.pghaz.revery.notification.NotificationHandler


class ReveryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHandler.createNotificationChannel(this)
    }
}