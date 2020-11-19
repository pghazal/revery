package com.pghaz.revery.model.room

import androidx.annotation.NonNull
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.pghaz.revery.repository.ReveryDatabase
import com.pghaz.revery.repository.converter.RoomTypeConverters

@Entity(tableName = ReveryDatabase.TABLE_NAME_TIMER)
@TypeConverters(RoomTypeConverters::class)
data class RTimer(

    @NonNull
    @PrimaryKey
    var id: Long = NO_ID,
    var label: String = "",
    var vibrate: Boolean = false,
    var fadeOut: Boolean = false,
    var fadeOutDuration: Long = 0,
    @Embedded var metadata: RMediaMetadata = RMediaMetadata(),
    var duration: Long = 0,
    var startTime: Long = 0,
    var stopTime: Long = 0,
    var remainingTime: Long = 0,
    var extraTime: Long = 0,
    var state: RTimerState = RTimerState.CREATED
) : RBaseModel()