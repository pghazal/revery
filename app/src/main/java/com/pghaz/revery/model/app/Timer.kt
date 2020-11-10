package com.pghaz.revery.model.app

import android.os.Parcelable
import com.pghaz.revery.model.room.RTimer
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Timer(
    var id: Long = NO_ID,
    var durationInSeconds: Int = 0,
    var label: String = "",
    var enabled: Boolean = true,
    var vibrate: Boolean = false,
    var fadeOut: Boolean = false,
    var fadeOutDuration: Long = 0,
    var isSnooze: Boolean = false,
    var isPreview: Boolean = false,
    var metadata: TimerMetadata = TimerMetadata()
) : BaseModel(), Parcelable {

    constructor(timer: Timer) : this(
        id = timer.id,
        durationInSeconds = timer.durationInSeconds,
        label = timer.label,
        enabled = timer.enabled,
        vibrate = timer.vibrate,
        fadeOut = timer.fadeOut,
        fadeOutDuration = timer.fadeOutDuration,
        isSnooze = timer.isSnooze,
        isPreview = timer.isPreview,
        metadata = TimerMetadata(timer.metadata)
    )

    companion object {
        fun fromDatabaseModel(timer: RTimer): Timer {
            return Timer(
                id = timer.id,
                durationInSeconds = timer.durationInSeconds,
                label = timer.label,
                enabled = timer.enabled,
                vibrate = timer.vibrate,
                fadeOut = timer.fadeOut,
                fadeOutDuration = timer.fadeOutDuration,
                isSnooze = false,
                isPreview = false,
                metadata = TimerMetadata.fromDatabaseModel(timer.metadata)
            )
        }

        fun toDatabaseModel(timer: Timer): RTimer {
            return RTimer(
                id = timer.id,
                durationInSeconds = timer.durationInSeconds,
                label = timer.label,
                enabled = timer.enabled,
                vibrate = timer.vibrate,
                fadeOut = timer.fadeOut,
                fadeOutDuration = timer.fadeOutDuration,
                metadata = TimerMetadata.toDatabaseModel(timer.metadata)
            )
        }
    }
}