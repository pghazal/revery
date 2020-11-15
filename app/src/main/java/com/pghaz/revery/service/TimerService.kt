package com.pghaz.revery.service

import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService

class TimerService : LifecycleService() {

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)



        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}