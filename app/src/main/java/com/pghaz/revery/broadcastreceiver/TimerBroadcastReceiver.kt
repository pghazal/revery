package com.pghaz.revery.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.pghaz.revery.model.app.Timer
import com.pghaz.revery.notification.NotificationHandler
import com.pghaz.revery.service.TimerService
import com.pghaz.revery.util.IntentUtils

class TimerBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val ACTION_TIMER_IS_OVER = "com.pghaz.revery.ACTION_TIMER_IS_OVER"

        fun getTimerIsOverActionIntent(context: Context?, timer: Timer): Intent {
            val intent = Intent(context?.applicationContext, TimerBroadcastReceiver::class.java)
            intent.action = ACTION_TIMER_IS_OVER

            // This is a workaround due to problems with Parcelables into Intent
            // See: https://stackoverflow.com/questions/39478422/pendingintent-getbroadcast-lost-parcelable-data
            IntentUtils.safePutTimerIntoIntent(intent, timer)

            return intent
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) {
            return
        }

        if (ACTION_TIMER_IS_OVER == intent.action) {
            val timer = IntentUtils.safeGetTimerFromIntent(intent)

            NotificationHandler.cancel(context, timer.id.toInt())

            if (timer.remainingTime == 0L) {
                Toast.makeText(context, "${timer.id}", Toast.LENGTH_SHORT).show()

                startTimerService(context, timer)
            }
        }
    }

    private fun startTimerService(context: Context, timer: Timer) {
        val service = Intent(context, TimerService::class.java)

        IntentUtils.safePutTimerIntoIntent(service, timer)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(service)
        } else {
            context.startService(service)
        }
    }
}