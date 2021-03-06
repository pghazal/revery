package com.pghaz.revery.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.pghaz.revery.model.app.Timer
import com.pghaz.revery.service.TimerRingingService
import com.pghaz.revery.service.TimerRunningService
import com.pghaz.revery.util.Arguments
import com.pghaz.revery.util.IntentUtils

class TimerBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val ACTION_TIMER_START = "com.pghaz.revery.ACTION_TIMER_START"
        private const val ACTION_TIMER_IS_OVER = "com.pghaz.revery.ACTION_TIMER_IS_OVER"
        private const val ACTION_TIMER_RUNNING_STOP = "com.pghaz.revery.ACTION_TIMER_RUNNING_STOP"
        private const val ACTION_TIMER_RUNNING_INCREMENT =
            "com.pghaz.revery.ACTION_TIMER_RUNNING_INCREMENT"

        private const val ACTION_TIMER_RINGING_STOP = "com.pghaz.revery.ACTION_TIMER_RINGING_STOP"
        private const val ACTION_TIMER_RINGING_INCREMENT =
            "com.pghaz.revery.ACTION_TIMER_RINGING_INCREMENT"

        fun buildStartTimerActionIntent(context: Context?, timer: Timer): Intent {
            val intent = Intent(context?.applicationContext, TimerBroadcastReceiver::class.java)
            intent.action = ACTION_TIMER_START

            IntentUtils.safePutTimerIntoIntent(intent, timer)

            return intent
        }

        fun buildTimerIsOverActionIntent(context: Context?, timer: Timer): Intent {
            val intent = Intent(context?.applicationContext, TimerBroadcastReceiver::class.java)
            intent.action = ACTION_TIMER_IS_OVER

            // This is a workaround due to problems with Parcelables into Intent
            // See: https://stackoverflow.com/questions/39478422/pendingintent-getbroadcast-lost-parcelable-data
            IntentUtils.safePutTimerIntoIntent(intent, timer)

            return intent
        }

        fun buildStopRingingTimerActionIntent(context: Context?, timer: Timer): Intent {
            val intent = Intent(context?.applicationContext, TimerBroadcastReceiver::class.java)
            intent.action = ACTION_TIMER_RINGING_STOP

            IntentUtils.safePutTimerIntoIntent(intent, timer)

            return intent
        }

        fun buildStopRunningTimerActionIntent(context: Context?, timer: Timer): Intent {
            val intent = Intent(context?.applicationContext, TimerBroadcastReceiver::class.java)
            intent.action = ACTION_TIMER_RUNNING_STOP

            IntentUtils.safePutTimerIntoIntent(intent, timer)

            return intent
        }

        fun buildRunningTimerIncrementActionIntent(
            context: Context?,
            timer: Timer,
            incrementValue: Int
        ): Intent {
            val intent = Intent(context?.applicationContext, TimerBroadcastReceiver::class.java)
            intent.action = ACTION_TIMER_RUNNING_INCREMENT

            IntentUtils.safePutTimerIntoIntent(intent, timer)

            intent.putExtra(Arguments.ARGS_TIMER_INCREMENT, incrementValue)

            return intent
        }

        fun buildRingingTimerIncrementActionIntent(
            context: Context?,
            timer: Timer,
            incrementValue: Int
        ): Intent {
            val intent = Intent(context?.applicationContext, TimerBroadcastReceiver::class.java)
            intent.action = ACTION_TIMER_RINGING_INCREMENT

            IntentUtils.safePutTimerIntoIntent(intent, timer)

            intent.putExtra(Arguments.ARGS_TIMER_INCREMENT, incrementValue)

            return intent
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) {
            return
        }

        if (ACTION_TIMER_START == intent.action) {
            val timer = IntentUtils.safeGetTimerFromIntent(intent)
            startTimerRunningService(context, timer)
        } else if (ACTION_TIMER_IS_OVER == intent.action) {
            val timer = IntentUtils.safeGetTimerFromIntent(intent)
            startTimerOverService(context, timer)
        } else if (ACTION_TIMER_RUNNING_INCREMENT == intent.action) {
            val timer = IntentUtils.safeGetTimerFromIntent(intent)
            val incrementValue = intent.getIntExtra(Arguments.ARGS_TIMER_INCREMENT, 0)
            broadcastRunningTimerIncrement(context, timer, incrementValue)
        } else if (ACTION_TIMER_RINGING_INCREMENT == intent.action) {
            val timer = IntentUtils.safeGetTimerFromIntent(intent)
            val incrementValue = intent.getIntExtra(Arguments.ARGS_TIMER_INCREMENT, 0)
            broadcastRingingTimerIncrement(context, timer, incrementValue)
        } else if (ACTION_TIMER_RINGING_STOP == intent.action) {
            val timer = IntentUtils.safeGetTimerFromIntent(intent)
            broadcastRingingTimerShouldStop(context, timer)
        } else if (ACTION_TIMER_RUNNING_STOP == intent.action) {
            val timer = IntentUtils.safeGetTimerFromIntent(intent)
            broadcastRunningTimerShouldStop(context, timer)
        }
    }

    private fun startTimerOverService(context: Context, timer: Timer) {
        val service = Intent(context, TimerRingingService::class.java)

        IntentUtils.safePutTimerIntoIntent(service, timer)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(service)
        } else {
            context.startService(service)
        }
    }

    private fun startTimerRunningService(context: Context, timer: Timer) {
        val service = Intent(context, TimerRunningService::class.java)

        IntentUtils.safePutTimerIntoIntent(service, timer)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(service)
        } else {
            context.startService(service)
        }
    }

    private fun broadcastRingingTimerShouldStop(context: Context, timer: Timer) {
        val timerShouldStopIntent =
            TimerRingingService.buildRingingTimerShouldStopIntent(context, timer)
        LocalBroadcastManager.getInstance(context).sendBroadcast(timerShouldStopIntent)
    }

    private fun broadcastRunningTimerShouldStop(context: Context, timer: Timer) {
        val timerShouldStopIntent =
            TimerRunningService.buildRunningTimerShouldStopIntent(context, timer)
        LocalBroadcastManager.getInstance(context).sendBroadcast(timerShouldStopIntent)
    }

    private fun broadcastRingingTimerIncrement(
        context: Context,
        timer: Timer,
        incrementValue: Int
    ) {
        val intent = TimerRingingService.buildRingingTimerIncrementIntent(context, timer)
        intent.putExtra(Arguments.ARGS_TIMER_INCREMENT, incrementValue)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun broadcastRunningTimerIncrement(
        context: Context,
        timer: Timer,
        incrementValue: Int
    ) {
        val intent = TimerRunningService.buildRunningTimerIncrementIntent(context, timer)
        intent.putExtra(Arguments.ARGS_TIMER_INCREMENT, incrementValue)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}