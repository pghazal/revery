package com.pghaz.revery.alarm.model.room

import androidx.annotation.NonNull
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pghaz.revery.alarm.model.RBaseModel
import com.pghaz.revery.alarm.repository.AlarmDatabase

@Entity(tableName = AlarmDatabase.ALARM_TABLE_NAME)
data class RAlarm(

    @NonNull
    @PrimaryKey
    var id: Long = 0,
    var hour: Int = 0,
    var minute: Int = 0,
    var label: String = "",
    var enabled: Boolean = true,
    var recurring: Boolean = false,
    var monday: Boolean = false,
    var tuesday: Boolean = false,
    var wednesday: Boolean = false,
    var thursday: Boolean = false,
    var friday: Boolean = false,
    var saturday: Boolean = false,
    var sunday: Boolean = false,
    var vibrate: Boolean = false,
    var fadeIn: Boolean = false,
    var fadeInDuration: Long = 0,
    @Embedded var metadata: RAlarmMetadata = RAlarmMetadata()
) : RBaseModel()