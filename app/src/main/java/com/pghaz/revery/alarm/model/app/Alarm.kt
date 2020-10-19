package com.pghaz.revery.alarm.model.app

import android.os.Parcelable
import com.pghaz.revery.alarm.model.room.RAlarm
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Alarm(
    override var id: Long = NO_ID,
    override var hour: Int = 0,
    override var minute: Int = 0,
    override var label: String = "",
    override var enabled: Boolean = true,
    override var recurring: Boolean = false,
    override var monday: Boolean = false,
    override var tuesday: Boolean = false,
    override var wednesday: Boolean = false,
    override var thursday: Boolean = false,
    override var friday: Boolean = false,
    override var saturday: Boolean = false,
    override var sunday: Boolean = false,
    override var vibrate: Boolean = false,
    override var fadeIn: Boolean = false,
    override var fadeInDuration: Long = 0,
    override var uri: String? = null
) : AbstractAlarm(), Parcelable {

    constructor(alarm: AbstractAlarm) : this(
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
        uri = alarm.uri
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
                uri = alarm.uri
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
                uri = alarm.uri
            )
        }
    }
}