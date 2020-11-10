package com.pghaz.revery.model.room

import androidx.annotation.NonNull
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pghaz.revery.repository.ReveryDatabase

@Entity(tableName = ReveryDatabase.TABLE_NAME_TIMER)
data class RTimer(

    @NonNull
    @PrimaryKey
    var id: Long = NO_ID,
    var durationInSeconds: Int = 0,
    var label: String = "",
    var enabled: Boolean = true,
    var vibrate: Boolean = false,
    var fadeOut: Boolean = false,
    var fadeOutDuration: Long = 0,
    @Embedded var metadata: RMediaMetadata = RMediaMetadata()
) : RBaseModel()