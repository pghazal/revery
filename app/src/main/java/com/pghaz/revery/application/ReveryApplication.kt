package com.pghaz.revery.application

import android.app.Application
import com.pghaz.revery.notification.NotificationHandler

/**
 * Declared in AndroidManifest.xml
 */
class ReveryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHandler.createNotificationChannels(this)
    }
}