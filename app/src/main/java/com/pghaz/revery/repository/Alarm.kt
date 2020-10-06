package com.pghaz.revery.repository

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = AlarmDatabase.ALARM_TABLE_NAME)
data class Alarm(
    @PrimaryKey
    var id: Long,
    var hour: Int,
    var minute: Int,
    var label: String = "",
    var recurring: Boolean = false,
    var enabled: Boolean = true,
    var monday: Boolean = false,
    var tuesday: Boolean = false,
    var wednesday: Boolean = false,
    var thursday: Boolean = false,
    var friday: Boolean = false,
    var saturday: Boolean = false,
    var sunday: Boolean = false,
    var vibrate: Boolean = false,
) {

    companion object {
        const val MONDAY = "MONDAY"
        const val TUESDAY = "TUESDAY"
        const val WEDNESDAY = "WEDNESDAY"
        const val THURSDAY = "THURSDAY"
        const val FRIDAY = "FRIDAY"
        const val SATURDAY = "SATURDAY"
        const val SUNDAY = "SUNDAY"
        const val RECURRING = "RECURRING"
        const val ENABLED = "ENABLED"
        const val LABEL = "LABEL"
        const val HOUR = "HOUR"
        const val MINUTE = "MINUTE"
        const val ID = "ID"
        const val VIBRATE = "VIBRATE"
    }
}