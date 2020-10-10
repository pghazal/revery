package com.pghaz.revery.alarm.model.room

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pghaz.revery.alarm.repository.AlarmDatabase

@Entity(tableName = AlarmDatabase.ALARM_TABLE_NAME)
data class RAlarm(
    @PrimaryKey
    var id: Long,
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
    @Embedded var metadata: RAlarmMetadata = RAlarmMetadata()
)