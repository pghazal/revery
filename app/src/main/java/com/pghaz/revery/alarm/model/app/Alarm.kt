package com.pghaz.revery.alarm.model.app

import com.pghaz.revery.alarm.model.room.RAlarm

data class Alarm(
    var id: Long = NO_ID,
    var hour: Int = 0,
    var minute: Int = 0,
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
    var metadata: AlarmMetadata? = null
) {

    companion object {
        fun fromDatabaseModel(alarm: RAlarm): Alarm {
            val metadata = AlarmMetadata.fromDatabaseModel(alarm.metadata)

            return Alarm(
                alarm.id, alarm.hour, alarm.minute, alarm.label, alarm.recurring,
                alarm.enabled, alarm.monday, alarm.tuesday, alarm.wednesday, alarm.thursday,
                alarm.friday, alarm.saturday, alarm.sunday, alarm.vibrate, metadata
            )
        }

        fun toDatabaseModel(alarm: Alarm): RAlarm {
            val metadata = AlarmMetadata.toDatabaseModel(alarm.metadata)

            return RAlarm(
                alarm.id, alarm.hour, alarm.minute, alarm.label, alarm.recurring, alarm.enabled,
                alarm.monday, alarm.tuesday, alarm.wednesday, alarm.thursday, alarm.friday,
                alarm.saturday, alarm.sunday, alarm.vibrate, metadata
            )
        }

        const val NO_ID: Long = 0

        const val ID = "ID"
        const val HOUR = "HOUR"
        const val MINUTE = "MINUTE"
        const val LABEL = "LABEL"
        const val RECURRING = "RECURRING"
        const val ENABLED = "ENABLED"
        const val MONDAY = "MONDAY"
        const val TUESDAY = "TUESDAY"
        const val WEDNESDAY = "WEDNESDAY"
        const val THURSDAY = "THURSDAY"
        const val FRIDAY = "FRIDAY"
        const val SATURDAY = "SATURDAY"
        const val SUNDAY = "SUNDAY"
        const val VIBRATE = "VIBRATE"
        const val METADATA = "METADATA"
    }
}