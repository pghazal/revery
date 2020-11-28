package com.pghaz.revery.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import com.pghaz.revery.broadcastreceiver.TimerBroadcastReceiver
import com.pghaz.revery.model.app.Timer
import com.pghaz.revery.model.app.TimerState

object TimerHandler {

    const val ONE_MINUTE = 60 * 1000
    const val HALF_MINUTE = 30 * 1000

    fun startTimer(timer: Timer) {
        val now = System.currentTimeMillis()
        timer.startTime = now

        if (timer.stopTime == 0L) {
            timer.stopTime = now + timer.duration
        } else {
            timer.stopTime = now + timer.remainingTime
        }

        timer.state = TimerState.RUNNING
    }

    fun pauseTimer(timer: Timer) {
        val now = System.currentTimeMillis()

        // If time is over
        timer.remainingTime = if (now >= timer.stopTime) {
            0
        } else {
            timer.stopTime - now
        }

        timer.state = TimerState.PAUSED
    }

    fun resetTimer(timer: Timer) {
        timer.startTime = 0
        timer.stopTime = 0
        timer.remainingTime = timer.duration
        timer.extraTime = 0
        timer.state = TimerState.CREATED
    }

    fun incrementTimer(timer: Timer, incrementValue: Int) {
        timer.remainingTime += incrementValue
        timer.extraTime += incrementValue
    }

    fun getRemainingTime(timer: Timer): Long {
        val now = System.currentTimeMillis()
        val remainingTime: Long

        if (timer.state == TimerState.RINGING) {
            remainingTime = now - timer.stopTime
        } else if (timer.state == TimerState.RUNNING && now >= timer.stopTime) {
            remainingTime = now - timer.stopTime
            timer.state = TimerState.RINGING
        } else if (timer.state != TimerState.RUNNING && timer.stopTime != 0L) {
            remainingTime = timer.remainingTime
        } else if (timer.state != TimerState.RUNNING && timer.stopTime == 0L) {
            remainingTime = 0
        } else {
            remainingTime = timer.stopTime - now
        }

        return remainingTime
    }

    fun getFullDuration(timer: Timer): Long {
        return timer.duration + timer.extraTime
    }

    fun setAlarm(context: Context, timer: Timer) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

        alarmManager?.let {
            val intent = TimerBroadcastReceiver.buildTimerIsOverActionIntent(context, timer)

            val pendingIntent = PendingIntent.getBroadcast(
                context.applicationContext,
                timer.id.toInt(),
                intent,
                PendingIntent.FLAG_ONE_SHOT
            )

            it.setExact(AlarmManager.RTC_WAKEUP, timer.stopTime, pendingIntent)

            val startIntent = TimerBroadcastReceiver.buildStartTimerActionIntent(context, timer)
            context.sendBroadcast(startIntent)
        }
    }

    fun removeAlarm(context: Context, timer: Timer) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

        alarmManager?.let {
            val intent = TimerBroadcastReceiver.buildTimerIsOverActionIntent(context, timer)

            val pendingIntent = PendingIntent.getBroadcast(
                context.applicationContext,
                timer.id.toInt(),
                intent,
                PendingIntent.FLAG_ONE_SHOT
            )

            it.cancel(pendingIntent)

            val stopIntent =
                TimerBroadcastReceiver.buildStopRunningTimerActionIntent(context, timer)
            context.sendBroadcast(stopIntent)
        }
    }
}