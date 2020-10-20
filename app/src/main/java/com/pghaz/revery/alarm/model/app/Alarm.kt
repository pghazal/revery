package com.pghaz.revery.alarm.model.app

import android.os.Parcelable
import com.pghaz.revery.alarm.model.BaseModel
import com.pghaz.revery.alarm.model.room.RAlarm
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
        metadata = AlarmMetadata(alarm.metadata)
    )

    companion object {
        fun fromDatabaseModel(alarm: RAlarm): Alarm {
            val metadata = AlarmMetadata.fromDatabaseModel(alarm.metadata)

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
                metadata = metadata
            )
        }

        fun toDatabaseModel(alarm: Alarm): RAlarm {
            val metadata = AlarmMetadata.toDatabaseModel(alarm.metadata)

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
                metadata = metadata
            )
        }
    }
}