package com.pghaz.revery.repository.converter

import androidx.room.TypeConverter
import com.pghaz.revery.model.room.RMediaType
import com.pghaz.revery.model.room.RTimerState

class RoomTypeConverters {

    @TypeConverter
    fun deserializeMediaType(value: Int): RMediaType {
        return RMediaType.values()[value]
    }

    @TypeConverter
    fun serializeMediaType(value: RMediaType): Int {
        return value.ordinal
    }

    @TypeConverter
    fun deserializeTimerState(value: Int): RTimerState {
        return RTimerState.values()[value]
    }

    @TypeConverter
    fun serializeTimerState(value: RTimerState): Int {
        return value.ordinal
    }
}