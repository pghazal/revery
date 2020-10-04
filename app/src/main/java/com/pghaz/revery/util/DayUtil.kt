package com.pghaz.revery.util

import java.util.*

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
    }
}