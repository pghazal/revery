package com.pghaz.revery.alarm.model.room

import androidx.room.TypeConverters
import com.pghaz.revery.alarm.model.AlarmTypeConverters
import com.pghaz.revery.alarm.model.RBaseModel

@TypeConverters(AlarmTypeConverters::class)
data class RAlarmMetadata(
    var alarmType: RAlarmType = RAlarmType.DEFAULT,
    var metadataId: String? = null,
    var uri: String? = null,
    var href: String? = null,
    var type: String? = null,
    var name: String? = null,
    var description: String? = null,
    var imageUrl: String? = null
) : RBaseModel()