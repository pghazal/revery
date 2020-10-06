package com.pghaz.revery.util

import android.content.Context
import com.pghaz.revery.R
import com.pghaz.revery.repository.Alarm
import java.util.*
import kotlin.math.abs

class DayUtil {
    companion object {
        // TODO: get day string from resources
        @Throws(Exception::class)
        fun toDay(day: Int): String {
            when (day) {
                Calendar.MONDAY -> return "Monday"
                Calendar.TUESDAY -> return "Tuesday"
                Calendar.WEDNESDAY -> return "Wednesday"
                Calendar.THURSDAY -> return "Thursday"
                Calendar.FRIDAY -> return "Friday"
                Calendar.SATURDAY -> return "Saturday"
                Calendar.SUNDAY -> return "Sunday"
            }
            throw Exception("Could not locate day")
        }

        fun getTimeRemaining(alarm: Alarm): TimeRemainingInfo {
            val currentTimeInMillis = System.currentTimeMillis()

            val alarmCalendar: Calendar = Calendar.getInstance()
            alarmCalendar.timeInMillis = currentTimeInMillis
            alarmCalendar.set(Calendar.HOUR_OF_DAY, alarm.hour)
            alarmCalendar.set(Calendar.MINUTE, alarm.minute)
            alarmCalendar.set(Calendar.SECOND, 0)
            alarmCalendar.set(Calendar.MILLISECOND, 0)

            val nextAlarmCalendar = Calendar.getInstance()
            nextAlarmCalendar.timeInMillis = alarmCalendar.timeInMillis

            if (!alarm.recurring) {
                // if alarm time has already passed, increment day by 1
                if (alarmCalendar.timeInMillis <= currentTimeInMillis) {
                    incrementByOneDay(alarmCalendar)
                }

                nextAlarmCalendar.timeInMillis = alarmCalendar.timeInMillis
            } else {
                // We'll add in this list all future alarms and then we'll pick the closest to 'now'
                val allAlarmsInMillis = ArrayList<Long>()

                if (alarm.monday) {
                    nextAlarmCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    // if alarm time has already passed, increment days by 7 = 1 week
                    if (nextAlarmCalendar.timeInMillis <= currentTimeInMillis) {
                        incrementByOneWeek(nextAlarmCalendar)
                    }
                    allAlarmsInMillis.add(nextAlarmCalendar.timeInMillis)
                }
                nextAlarmCalendar.timeInMillis = alarmCalendar.timeInMillis

                if (alarm.tuesday) {
                    nextAlarmCalendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
                    // if alarm time has already passed, increment days by 7 = 1 week
                    if (nextAlarmCalendar.timeInMillis <= currentTimeInMillis) {
                        incrementByOneWeek(nextAlarmCalendar)
                    }
                    allAlarmsInMillis.add(nextAlarmCalendar.timeInMillis)
                }
                nextAlarmCalendar.timeInMillis = alarmCalendar.timeInMillis

                if (alarm.wednesday) {
                    nextAlarmCalendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
                    // if alarm time has already passed, increment days by 7 = 1 week
                    if (nextAlarmCalendar.timeInMillis <= currentTimeInMillis) {
                        incrementByOneWeek(nextAlarmCalendar)
                    }
                    allAlarmsInMillis.add(nextAlarmCalendar.timeInMillis)
                }
                nextAlarmCalendar.timeInMillis = alarmCalendar.timeInMillis

                if (alarm.thursday) {
                    nextAlarmCalendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
                    // if alarm time has already passed, increment days by 7 = 1 week
                    if (nextAlarmCalendar.timeInMillis <= currentTimeInMillis) {
                        incrementByOneWeek(nextAlarmCalendar)
                    }
                    allAlarmsInMillis.add(nextAlarmCalendar.timeInMillis)
                }
                nextAlarmCalendar.timeInMillis = alarmCalendar.timeInMillis

                if (alarm.friday) {
                    nextAlarmCalendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
                    // if alarm time has already passed, increment days by 7 = 1 week
                    if (nextAlarmCalendar.timeInMillis <= currentTimeInMillis) {
                        incrementByOneWeek(nextAlarmCalendar)
                    }
                    allAlarmsInMillis.add(nextAlarmCalendar.timeInMillis)
                }
                nextAlarmCalendar.timeInMillis = alarmCalendar.timeInMillis

                if (alarm.saturday) {
                    nextAlarmCalendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
                    // if alarm time has already passed, increment days by 7 = 1 week
                    if (nextAlarmCalendar.timeInMillis <= currentTimeInMillis) {
                        incrementByOneWeek(nextAlarmCalendar)
                    }
                    allAlarmsInMillis.add(nextAlarmCalendar.timeInMillis)
                }
                nextAlarmCalendar.timeInMillis = alarmCalendar.timeInMillis

                if (alarm.sunday) {
                    nextAlarmCalendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                    // if alarm time has already passed, increment days by 7 = 1 week
                    if (nextAlarmCalendar.timeInMillis <= currentTimeInMillis) {
                        incrementByOneWeek(nextAlarmCalendar)
                    }
                    allAlarmsInMillis.add(nextAlarmCalendar.timeInMillis)
                }

                // Get the closest next alarm this week compare to 'now'
                val closestTimeInMillis: Long = Collections.min(allAlarmsInMillis) { d1, d2 ->
                    val diff1: Long = abs(d1 - currentTimeInMillis)
                    val diff2: Long = abs(d2 - currentTimeInMillis)
                    diff1.compareTo(diff2)
                }

                nextAlarmCalendar.timeInMillis = closestTimeInMillis
            }

            val diff = nextAlarmCalendar.timeInMillis - currentTimeInMillis

            val diffSeconds = diff / 1000 % 60
            val diffMinutes = (diff / (60 * 1000) + 1) % 60
            val diffHours = diff / (60 * 60 * 1000) % 24
            val diffDays = diff / (24 * 60 * 60 * 1000)

            //val format = SimpleDateFormat("EEEE, MMMM d, yyyy 'at' h:mm a", Locale.getDefault())
            //return format.format(nextAlarmCalendar.time)

            return TimeRemainingInfo(diffDays.toInt(), diffHours.toInt(), diffMinutes.toInt())
        }

        fun incrementByOneDay(calendarToIncrement: Calendar) {
            calendarToIncrement.set(
                Calendar.DAY_OF_MONTH,
                calendarToIncrement.get(Calendar.DAY_OF_MONTH) + 1
            )
        }

        private fun incrementByOneWeek(calendarToIncrement: Calendar) {
            calendarToIncrement.set(
                Calendar.DAY_OF_MONTH,
                calendarToIncrement.get(Calendar.DAY_OF_MONTH) + 7
            )
        }

        private fun getPluralsString(context: Context, stringResId: Int, value: Int): String {
            return context.resources.getQuantityString(stringResId, value, value)
        }

        fun getRemainingTimeText(
            context: Context,
            timeRemainingInfo: TimeRemainingInfo
        ): String {
            val stringBuilder = StringBuilder()

            // Add days remaining
            if (timeRemainingInfo.days > 0) {
                stringBuilder.append(
                    getPluralsString(
                        context,
                        R.plurals.time_remaining_days,
                        timeRemainingInfo.days
                    )
                ).append(" ")
            }

            // add hours remaining
            if (timeRemainingInfo.hours > 0) {
                stringBuilder.append(
                    getPluralsString(
                        context,
                        R.plurals.time_remaining_hours,
                        timeRemainingInfo.hours
                    )
                ).append(" ")
            }

            // add minutes remaining
            if (timeRemainingInfo.minutes > 0) {
                stringBuilder.append(
                    getPluralsString(
                        context,
                        R.plurals.time_remaining_minutes,
                        timeRemainingInfo.minutes
                    )
                ).append(" ")
            }

            return stringBuilder.toString()
        }

        fun getCurrentHour(calendar: Calendar): Int {
            return calendar.get(Calendar.HOUR_OF_DAY)
        }

        fun getCurrentMinute(calendar: Calendar): Int {
            return calendar.get(Calendar.MINUTE)
        }
    }
}