package com.pghaz.revery.model.app

import android.os.Parcelable
import com.pghaz.revery.model.room.RTimer
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Timer(
    var id: Long = NO_ID,
    var hour: Int = 0,
    var minute: Int = 0,
    var second: Int = 0,
    var label: String = "",
    var enabled: Boolean = true,
    var vibrate: Boolean = false,
    var fadeOut: Boolean = false,
    var fadeOutDuration: Long = 0,
    var isSnooze: Boolean = false,
    var isPreview: Boolean = false,
    var metadata: MediaMetadata = MediaMetadata()
) : BaseModel(), Parcelable {

    constructor(timer: Timer) : this(
        id = timer.id,
        hour = timer.hour,
        minute = timer.minute,
        second = timer.second,
        label = timer.label,
        enabled = timer.enabled,
        vibrate = timer.vibrate,
        fadeOut = timer.fadeOut,
        fadeOutDuration = timer.fadeOutDuration,
        isSnooze = timer.isSnooze,
        isPreview = timer.isPreview,
        metadata = MediaMetadata(timer.metadata)
    )

    companion object {
        fun fromDatabaseModel(timer: RTimer): Timer {
            return Timer(
                id = timer.id,
                hour = timer.hour,
                minute = timer.minute,
                second = timer.second,
                label = timer.label,
                enabled = timer.enabled,
                vibrate = timer.vibrate,
                fadeOut = timer.fadeOut,
                fadeOutDuration = timer.fadeOutDuration,
                isSnooze = false,
                isPreview = false,
                metadata = MediaMetadata.fromDatabaseModel(timer.metadata)
            )
        }

        fun toDatabaseModel(timer: Timer): RTimer {
            return RTimer(
                id = timer.id,
                hour = timer.hour,
                minute = timer.minute,
                second = timer.second,
                label = timer.label,
                enabled = timer.enabled,
                vibrate = timer.vibrate,
                fadeOut = timer.fadeOut,
                fadeOutDuration = timer.fadeOutDuration,
                metadata = MediaMetadata.toDatabaseModel(timer.metadata)
            )
        }
    }
}