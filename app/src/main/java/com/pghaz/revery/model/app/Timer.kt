package com.pghaz.revery.model.app

import android.os.Parcelable
import com.pghaz.revery.model.room.RTimer
import com.pghaz.revery.model.room.RTimerState
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Timer(
    var id: Long = NO_ID,
    var label: String = "",
    var vibrate: Boolean = false,
    var fadeOut: Boolean = false,
    var fadeOutDuration: Long = 0,
    var metadata: MediaMetadata = MediaMetadata(),
    var duration: Long = 0,
    var startTime: Long = 0,
    var stopTime: Long = 0,
    var remainingTime: Long = 0,
    var extraTime: Long = 0,
    var state: TimerState = TimerState.CREATED
) : BaseModel(), Parcelable {

    constructor(timer: Timer) : this(
        id = timer.id,
        label = timer.label,
        vibrate = timer.vibrate,
        fadeOut = timer.fadeOut,
        fadeOutDuration = timer.fadeOutDuration,
        metadata = MediaMetadata(timer.metadata),
        duration = timer.duration,
        startTime = timer.startTime,
        stopTime = timer.stopTime,
        remainingTime = timer.remainingTime,
        extraTime = timer.extraTime,
        state = timer.state
    )

    companion object {
        fun fromDatabaseModel(timer: RTimer): Timer {
            return Timer(
                id = timer.id,
                label = timer.label,
                vibrate = timer.vibrate,
                fadeOut = timer.fadeOut,
                fadeOutDuration = timer.fadeOutDuration,
                metadata = MediaMetadata.fromDatabaseModel(timer.metadata),
                duration = timer.duration,
                startTime = timer.startTime,
                stopTime = timer.stopTime,
                remainingTime = timer.remainingTime,
                extraTime = timer.extraTime,
                state = TimerState.values()[timer.state.ordinal]
            )
        }

        fun toDatabaseModel(timer: Timer): RTimer {
            return RTimer(
                id = timer.id,
                label = timer.label,
                vibrate = timer.vibrate,
                fadeOut = timer.fadeOut,
                fadeOutDuration = timer.fadeOutDuration,
                metadata = MediaMetadata.toDatabaseModel(timer.metadata),
                duration = timer.duration,
                startTime = timer.startTime,
                stopTime = timer.stopTime,
                remainingTime = timer.remainingTime,
                extraTime = timer.extraTime,
                state = RTimerState.values()[timer.state.ordinal]
            )
        }
    }
}