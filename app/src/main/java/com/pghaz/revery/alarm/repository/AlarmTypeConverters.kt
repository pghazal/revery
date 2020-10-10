package com.pghaz.revery.alarm.repository

import androidx.room.TypeConverter
import com.pghaz.revery.alarm.model.room.RAlarmType

class AlarmTypeConverters {

    @TypeConverter
    fun toType(value: Int): RAlarmType {
        return RAlarmType.values()[value]
    }

    @TypeConverter
    fun fromType(value: RAlarmType): Int {
        return value.ordinal
    }
}