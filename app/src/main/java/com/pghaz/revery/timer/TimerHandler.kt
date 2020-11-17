package com.pghaz.revery.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import com.pghaz.revery.broadcastreceiver.TimerBroadcastReceiver
import com.pghaz.revery.model.app.Timer
import com.pghaz.revery.model.app.TimerState

object TimerHandler {

    private const val ONE_MINUTE = 60 * 1000

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
        timer.remainingTime = 0
        timer.state = TimerState.CREATED
    }

    fun incrementTimer(timer: Timer) {
        timer.remainingTime += ONE_MINUTE
    }

    fun getElapsedTime(timer: Timer): Long {
        val now = System.currentTimeMillis()
        val elapsedTime: Long

        if (timer.state == TimerState.RINGING) {
            elapsedTime = now - timer.stopTime
        } else if (timer.state == TimerState.RUNNING && now >= timer.stopTime) {
            elapsedTime = now - timer.stopTime
            timer.state = TimerState.RINGING
        } else if (timer.state != TimerState.RUNNING && timer.stopTime != 0L) {
            elapsedTime = timer.remainingTime
        } else if (timer.state != TimerState.RUNNING && timer.stopTime == 0L) {
            elapsedTime = 0
        } else {
            elapsedTime = timer.stopTime - now
        }

        return elapsedTime
    }

    fun setAlarm(context: Context?, timer: Timer) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

        alarmManager?.let {
            val intent = TimerBroadcastReceiver.getTimerIsOverActionIntent(context, timer)

            val pendingIntent = PendingIntent.getBroadcast(
                context?.applicationContext,
                timer.id.toInt(),
                intent,
                PendingIntent.FLAG_ONE_SHOT
            )

            it.setExact(AlarmManager.RTC_WAKEUP, timer.stopTime, pendingIntent)
        }
    }

    fun removeAlarm(context: Context?, timer: Timer) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

        alarmManager?.let {
            val intent = TimerBroadcastReceiver.getTimerIsOverActionIntent(context, timer)

            val pendingIntent = PendingIntent.getBroadcast(
                context?.applicationContext,
                timer.id.toInt(),
                intent,
                PendingIntent.FLAG_ONE_SHOT
            )

            it.cancel(pendingIntent)
        }
    }
}