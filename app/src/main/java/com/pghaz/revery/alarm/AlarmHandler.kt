package com.pghaz.revery.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.widget.Toast
import com.pghaz.revery.BuildConfig
import com.pghaz.revery.alarm.broadcastreceiver.AlarmBroadcastReceiver
import com.pghaz.revery.alarm.model.app.AbstractAlarm
import com.pghaz.revery.alarm.model.app.Alarm
import com.pghaz.revery.alarm.model.app.SpotifyAlarm
import com.pghaz.revery.util.DateTimeUtils
import java.util.*

object AlarmHandler {

    // This is for test purpose only
    fun fireAlarmNow(
        context: Context?,
        delayInSeconds: Int,
        recurring: Boolean,
        spotify: Boolean,
        fadeIn: Boolean = false,
        fadeInDuration: Long = 0,
        vibrate: Boolean = false
    ) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        // add delay in seconds
        calendar.add(Calendar.SECOND, delayInSeconds)

        val hour = DateTimeUtils.getCurrentHourOfDay(calendar)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        val uri: String? = if (spotify) {
            "spotify:playlist:3H8dsoJvkH7lUkaQlUNjPJ"
        } else {
            null
        }

        val alarm = Alarm(
            id = System.currentTimeMillis(),
            hour = hour,
            minute = minute,
            recurring = recurring,
            monday = true,
            tuesday = true,
            wednesday = true,
            thursday = true,
            friday = true,
            saturday = true,
            sunday = true,
            vibrate = vibrate,
            fadeIn = fadeIn,
            fadeInDuration = fadeInDuration,
            uri = uri
        )

        scheduleAlarm(context, alarm, alarm.minute, second)
    }

    fun scheduleAlarm(context: Context?, alarm: AbstractAlarm) {
        scheduleAlarm(context, alarm, alarm.minute, 0)
    }

    private fun scheduleAlarm(
        context: Context?,
        alarm: AbstractAlarm,
        minute: Int,
        second: Int
    ) {
        val alarmManager =
            context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

        alarmManager?.let {
            val intent = AlarmBroadcastReceiver.getScheduleAlarmActionIntent(context, alarm)

            val alarmPendingIntent =
                PendingIntent.getBroadcast(
                    context?.applicationContext,
                    alarm.id.toInt(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

            val now = System.currentTimeMillis()

            val calendar: Calendar = Calendar.getInstance()
            calendar.timeInMillis = now
            calendar.set(Calendar.HOUR_OF_DAY, alarm.hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, second)
            calendar.set(Calendar.MILLISECOND, 0)

            // if alarm time has already passed, increment day by 1
            if (calendar.timeInMillis <= now) {
                DateTimeUtils.incrementByOneDay(calendar)
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                alarmPendingIntent
            )

            alarm.enabled = true

            if (BuildConfig.DEBUG) {
                val toastText = String.format(
                    Locale.getDefault(),
                    "Alarm scheduled for %s at %02d:%02d with id %d. Recurring: %s",
                    DateTimeUtils.toDay(calendar[Calendar.DAY_OF_WEEK]),
                    alarm.hour,
                    alarm.minute,
                    alarm.id,
                    DateTimeUtils.getDaysText(alarm)
                )
                Toast.makeText(context, toastText, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun cancelAlarm(context: Context?, alarm: AbstractAlarm) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

        alarmManager?.let {
            val intent = AlarmBroadcastReceiver.getScheduleAlarmActionIntent(context, alarm)

            val alarmPendingIntent =
                PendingIntent.getBroadcast(
                    context?.applicationContext,
                    alarm.id.toInt(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            it.cancel(alarmPendingIntent)

            disableAlarm(alarm)
        }
    }

    fun disableAlarm(alarm: AbstractAlarm) {
        alarm.enabled = false
    }

    fun snooze(context: Context?, alarm: AbstractAlarm, delayInMinutes: Int) {
        val now = System.currentTimeMillis()

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = now
        // add delay in minutes
        calendar.add(Calendar.MINUTE, delayInMinutes)

        val hour = DateTimeUtils.getCurrentHourOfDay(calendar)
        val minute = calendar.get(Calendar.MINUTE)

        val snoozeAlarm = when (alarm) {
            is Alarm -> {
                Alarm(alarm)
            }

            is SpotifyAlarm -> {
                SpotifyAlarm(alarm, alarm.name, alarm.description, alarm.imageUrl)
            }

            else -> {
                throw IllegalArgumentException("AlarmHandler: snooze alarm type invalid")
            }
        }

        snoozeAlarm.id = now
        snoozeAlarm.recurring = false
        snoozeAlarm.enabled = true
        snoozeAlarm.hour = hour
        snoozeAlarm.minute = minute

        scheduleAlarm(context, snoozeAlarm)
    }
}