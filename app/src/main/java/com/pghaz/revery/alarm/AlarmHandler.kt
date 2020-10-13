package com.pghaz.revery.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
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

        scheduleAlarm(context, alarm, second + delayInSeconds)
    }

    fun scheduleAlarm(context: Context?, alarm: Alarm) {
        // TODO: idea feature: set an end time to alarm playing

        scheduleAlarm(context, alarm, 0)
    }

    private fun scheduleAlarm(context: Context?, alarm: Alarm, delayInSeconds: Int) {
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
            calendar.set(Calendar.MINUTE, alarm.minute)
            calendar.set(Calendar.SECOND, delayInSeconds)
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
}