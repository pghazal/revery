package com.pghaz.revery.repository.converter

import androidx.room.TypeConverter
import com.pghaz.revery.model.room.RMediaType

class MediaTypeConverters {

    @TypeConverter
    fun deserializeMediaType(value: Int): RMediaType {
        return RMediaType.values()[value]
    }

    @TypeConverter
    fun serializeMediaType(value: RMediaType): Int {
        return value.ordinal
    }
}