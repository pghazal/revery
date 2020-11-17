package com.pghaz.revery.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.pghaz.revery.model.app.Timer
import com.pghaz.revery.notification.NotificationHandler
import com.pghaz.revery.service.TimerOverService
import com.pghaz.revery.util.Arguments
import com.pghaz.revery.util.IntentUtils

class TimerBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val ACTION_TIMER_IS_OVER = "com.pghaz.revery.ACTION_TIMER_IS_OVER"
        private const val ACTION_TIMER_STOP = "com.pghaz.revery.ACTION_TIMER_STOP"
        private const val ACTION_TIMER_INCREMENT = "com.pghaz.revery.ACTION_TIMER_INCREMENT"

        fun getTimerIsOverActionIntent(context: Context?, timer: Timer): Intent {
            val intent = Intent(context?.applicationContext, TimerBroadcastReceiver::class.java)
            intent.action = ACTION_TIMER_IS_OVER

            // This is a workaround due to problems with Parcelables into Intent
            // See: https://stackoverflow.com/questions/39478422/pendingintent-getbroadcast-lost-parcelable-data
            IntentUtils.safePutTimerIntoIntent(intent, timer)

            return intent
        }

        fun getStopTimerActionIntent(context: Context?, timer: Timer): Intent {
            val intent = Intent(context?.applicationContext, TimerBroadcastReceiver::class.java)
            intent.action = ACTION_TIMER_STOP

            IntentUtils.safePutTimerIntoIntent(intent, timer)

            return intent
        }

        fun getTimerIncrementActionIntent(context: Context?, timer: Timer): Intent {
            val intent = Intent(context?.applicationContext, TimerBroadcastReceiver::class.java)
            intent.action = ACTION_TIMER_INCREMENT

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

            Toast.makeText(context, "${timer.id}", Toast.LENGTH_SHORT).show()
            startTimerOverService(context, timer)
        } else if (ACTION_TIMER_STOP == intent.action) {
            val timer = IntentUtils.safeGetTimerFromIntent(intent)
            val isIncrementAction = intent.getBooleanExtra(Arguments.ARGS_TIMER_INCREMENT, false)
            broadcastServiceTimerShouldStop(context, timer, isIncrementAction)
        } else if (ACTION_TIMER_INCREMENT == intent.action) {
            val timer = IntentUtils.safeGetTimerFromIntent(intent)
            broadcastServiceTimerIncrement(context, timer)
        }
    }

    private fun startTimerOverService(context: Context, timer: Timer) {
        val service = Intent(context, TimerOverService::class.java)

        IntentUtils.safePutTimerIntoIntent(service, timer)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(service)
        } else {
            context.startService(service)
        }
    }

    private fun broadcastServiceTimerShouldStop(
        context: Context,
        timer: Timer,
        isIncrementAction: Boolean
    ) {
        val timerShouldStopIntent = TimerOverService.getTimerShouldStopIntent(context, timer)
        timerShouldStopIntent.putExtra(Arguments.ARGS_TIMER_INCREMENT, isIncrementAction)
        LocalBroadcastManager.getInstance(context).sendBroadcast(timerShouldStopIntent)
    }

    private fun broadcastServiceTimerIncrement(context: Context, timer: Timer) {
        val timerIncrementIntent = TimerOverService.getTimerIncrementIntent(context, timer)
        LocalBroadcastManager.getInstance(context).sendBroadcast(timerIncrementIntent)
    }
}