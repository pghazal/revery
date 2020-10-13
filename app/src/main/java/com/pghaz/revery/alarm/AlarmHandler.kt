package com.pghaz.revery.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.pghaz.revery.BuildConfig
import com.pghaz.revery.alarm.broadcastreceiver.AlarmBroadcastReceiver
import com.pghaz.revery.alarm.model.app.Alarm
import com.pghaz.revery.alarm.model.app.AlarmMetadata
import com.pghaz.revery.alarm.model.room.RAlarmType
import com.pghaz.revery.util.Arguments
import com.pghaz.revery.util.DayUtil
import java.util.*

object AlarmHandler {

    private const val RUN_DAILY = 24 * 60 * 60 * 1000.toLong()

    // This is for test purpose only
    fun fireAlarmNow(
        context: Context?,
        delayInSeconds: Int,
        recurring: Boolean,
        spotify: Boolean,
        fadeIn: Boolean = false,
        fadeInDuration: Long = 0
    ) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        // add delay in seconds
        calendar.add(Calendar.SECOND, delayInSeconds)

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        val metadata = AlarmMetadata()
        if (spotify) {
            metadata.type = RAlarmType.SPOTIFY
            metadata.uri = "spotify:playlist:3H8dsoJvkH7lUkaQlUNjPJ"
        } else {
            metadata.type = RAlarmType.DEFAULT
        }

        val alarm = Alarm(
            id = System.currentTimeMillis(),
            hour = hour,
            minute = minute,
            recurring = recurring,
            fadeIn = fadeIn,
            fadeInDuration = fadeInDuration,
            metadata = metadata
        )

        scheduleAlarm(context, alarm, alarm.minute, second)
    }

    fun scheduleAlarm(context: Context?, alarm: Alarm) {
        scheduleAlarm(context, alarm, alarm.minute, 0)
    }

    private fun scheduleAlarm(
        context: Context?,
        alarm: Alarm,
        minute: Int,
        second: Int
    ) {
        val alarmManager =
            context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

        alarmManager?.let {
            val intent = Intent(context?.applicationContext, AlarmBroadcastReceiver::class.java)

            // This is a workaround due to problems with Parcelables into Intent
            // See: https://stackoverflow.com/questions/39478422/pendingintent-getbroadcast-lost-parcelable-data
            val alarmBundle = Bundle()
            alarmBundle.putParcelable(Arguments.ARGS_ALARM, alarm)
            intent.putExtra(Arguments.ARGS_BUNDLE_ALARM, alarmBundle)

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
                DayUtil.incrementByOneDay(calendar)
            }

            if (!alarm.recurring) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    alarmPendingIntent
                )
            } else {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    RUN_DAILY,
                    alarmPendingIntent
                )
            }

            alarm.enabled = true

            if (BuildConfig.DEBUG) {
                val toastText = String.format(
                    Locale.getDefault(),
                    "Alarm scheduled for %s at %02d:%02d with id %d",
                    DayUtil.getDaysText(calendar[Calendar.DAY_OF_WEEK], alarm),
                    alarm.hour,
                    alarm.minute,
                    alarm.id
                )
                Toast.makeText(context, toastText, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun cancelAlarm(context: Context?, alarm: Alarm) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

        alarmManager?.let {
            val intent = Intent(context?.applicationContext, AlarmBroadcastReceiver::class.java)
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

    fun disableAlarm(alarm: Alarm) {
        alarm.enabled = false
    }

    fun snooze(context: Context?, alarm: Alarm, delayInMinutes: Int) {
        val now = System.currentTimeMillis()

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = now
        // add delay in minutes
        calendar.add(Calendar.MINUTE, delayInMinutes)

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val snoozeAlarm =
            alarm.copy(id = now, recurring = false, enabled = true, hour = hour, minute = minute)

        scheduleAlarm(context, snoozeAlarm, snoozeAlarm.minute, 0)
    }
}