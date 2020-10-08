package com.pghaz.revery.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.pghaz.revery.broadcastreceiver.AlarmBroadcastReceiver
import com.pghaz.revery.repository.Alarm
import com.pghaz.revery.util.DayUtil
import java.util.*

class AlarmHandler {

    companion object {
        private const val RUN_DAILY = 24 * 60 * 60 * 1000.toLong() // 1 day
    }

    fun scheduleAlarm(context: Context?, alarm: Alarm) {
        val alarmManager =
            context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

        alarmManager?.let {
            val intent = Intent(context?.applicationContext, AlarmBroadcastReceiver::class.java)
            intent.putExtra(Alarm.MONDAY, alarm.monday)
            intent.putExtra(Alarm.TUESDAY, alarm.tuesday)
            intent.putExtra(Alarm.WEDNESDAY, alarm.wednesday)
            intent.putExtra(Alarm.THURSDAY, alarm.thursday)
            intent.putExtra(Alarm.FRIDAY, alarm.friday)
            intent.putExtra(Alarm.SATURDAY, alarm.saturday)
            intent.putExtra(Alarm.SUNDAY, alarm.sunday)

            intent.putExtra(Alarm.ID, alarm.id)
            intent.putExtra(Alarm.RECURRING, alarm.recurring)
            intent.putExtra(Alarm.LABEL, alarm.label)
            intent.putExtra(Alarm.VIBRATE, alarm.vibrate)

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
            calendar.set(Calendar.SECOND, 0)
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