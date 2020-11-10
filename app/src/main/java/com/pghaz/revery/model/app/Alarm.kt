package com.pghaz.revery.model.app

import android.os.Parcelable
import com.pghaz.revery.model.room.RAlarm
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Alarm(
    var id: Long = NO_ID,
    var hour: Int = 0,
    var minute: Int = 0,
    var label: String = "",
    var enabled: Boolean = true,
    var recurring: Boolean = false,
    var monday: Boolean = false,
    var tuesday: Boolean = false,
    var wednesday: Boolean = false,
    var thursday: Boolean = false,
    var friday: Boolean = false,
    var saturday: Boolean = false,
    var sunday: Boolean = false,
    var vibrate: Boolean = false,
    var fadeIn: Boolean = false,
    var fadeInDuration: Long = 0,
    var isSnooze: Boolean = false,
    var isPreview: Boolean = false,
    var metadata: AlarmMetadata = AlarmMetadata()
) : BaseModel(), Parcelable {

    constructor(alarm: Alarm) : this(
        id = alarm.id,
        hour = alarm.hour,
        minute = alarm.minute,
        label = alarm.label,
        enabled = alarm.enabled,
        recurring = alarm.recurring,
        monday = alarm.monday,
        tuesday = alarm.tuesday,
        wednesday = alarm.wednesday,
        thursday = alarm.thursday,
        friday = alarm.friday,
        saturday = alarm.saturday,
        sunday = alarm.sunday,
        vibrate = alarm.vibrate,
        fadeIn = alarm.fadeIn,
        fadeInDuration = alarm.fadeInDuration,
        isSnooze = alarm.isSnooze,
        isPreview = alarm.isPreview,
        metadata = AlarmMetadata(alarm.metadata)
    )

    companion object {
        fun fromDatabaseModel(alarm: RAlarm): Alarm {
            return Alarm(
                id = alarm.id,
                hour = alarm.hour,
                minute = alarm.minute,
                label = alarm.label,
                enabled = alarm.enabled,
                recurring = alarm.recurring,
                monday = alarm.monday,
                tuesday = alarm.tuesday,
                wednesday = alarm.wednesday,
                thursday = alarm.thursday,
                friday = alarm.friday,
                saturday = alarm.saturday,
                sunday = alarm.sunday,
                vibrate = alarm.vibrate,
                fadeIn = alarm.fadeIn,
                fadeInDuration = alarm.fadeInDuration,
                isSnooze = false,
                isPreview = false,
                metadata = AlarmMetadata.fromDatabaseModel(alarm.metadata)
            )
        }

        fun toDatabaseModel(alarm: Alarm): RAlarm {
            return RAlarm(
                id = alarm.id,
                hour = alarm.hour,
                minute = alarm.minute,
                label = alarm.label,
                enabled = alarm.enabled,
                recurring = alarm.recurring,
                monday = alarm.monday,
                tuesday = alarm.tuesday,
                wednesday = alarm.wednesday,
                thursday = alarm.thursday,
                friday = alarm.friday,
                saturday = alarm.saturday,
                sunday = alarm.sunday,
                vibrate = alarm.vibrate,
                fadeIn = alarm.fadeIn,
                fadeInDuration = alarm.fadeInDuration,
                metadata = AlarmMetadata.toDatabaseModel(alarm.metadata)
            )
        }
    }
}