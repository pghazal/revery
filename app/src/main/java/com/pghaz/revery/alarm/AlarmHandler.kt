package com.pghaz.revery.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.pghaz.revery.BuildConfig
import com.pghaz.revery.broadcastreceiver.AlarmBroadcastReceiver
import com.pghaz.revery.repository.Alarm
import com.pghaz.revery.util.DayUtil
import java.util.*

class AlarmHandler {

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
                var toastText: String? = null
                try {
                    toastText = String.format(
                        Locale.getDefault(),
                        "One Time Alarm %s scheduled for %s at %02d:%02d with id %d",
                        alarm.label,
                        DayUtil.toDay(calendar[Calendar.DAY_OF_WEEK]),
                        alarm.hour,
                        alarm.minute,
                        alarm.id
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    alarmPendingIntent
                )

                Toast.makeText(context, toastText, Toast.LENGTH_LONG).show()

            } else {
                val toastText = String.format(
                    Locale.getDefault(),
                    "Recurring Alarm %s scheduled for %s at %02d:%02d with id %d",
                    alarm.label,
                    getRecurringDaysText(alarm),
                    alarm.hour,
                    alarm.minute,
                    alarm.id
                )

                // TODO: do not forget the debug value
                val RUN_DAILY = if (BuildConfig.DEBUG) {
                    60 * 1000.toLong() // 1 minute
                } else {
                    24 * 60 * 60 * 1000.toLong() // 1 day
                }

                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    RUN_DAILY,
                    alarmPendingIntent
                )

                Toast.makeText(context, toastText, Toast.LENGTH_LONG).show()
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

            val toastText = String.format(
                Locale.getDefault(),
                "Alarm cancelled for %02d:%02d with id %d",
                alarm.hour,
                alarm.minute,
                alarm.id
            )

            Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
        }
    }

    fun disableAlarm(alarm: Alarm) {
        alarm.enabled = false
    }

    // TODO: text of recurring days
    private fun getRecurringDaysText(alarm: Alarm): String? {
        if (!alarm.recurring) {
            return null
        }
        var days = ""
        if (alarm.monday) {
            days += "Mo "
        }
        if (alarm.tuesday) {
            days += "Tu "
        }
        if (alarm.wednesday) {
            days += "We "
        }
        if (alarm.thursday) {
            days += "Th "
        }
        if (alarm.friday) {
            days += "Fr "
        }
        if (alarm.saturday) {
            days += "Sa "
        }
        if (alarm.sunday) {
            days += "Su "
        }
        return days
    }
}