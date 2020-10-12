package com.pghaz.revery.alarm.model.room

import androidx.room.TypeConverters
import com.pghaz.revery.alarm.repository.AlarmTypeConverters

@TypeConverters(AlarmTypeConverters::class)
data class RAlarmMetadata(
    var type: RAlarmType = RAlarmType.DEFAULT,
    var name: String? = null,
    var uri: String? = null,
    var description: String? = null,
    var imageUrl: String? = null,
    var fadeIn: Boolean? = false,
    var fadeInDuration: Long? = 0
)