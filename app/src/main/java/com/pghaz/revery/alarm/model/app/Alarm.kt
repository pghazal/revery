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
        metadata = alarm.metadata
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Alarm) return false

        if (id != other.id) return false
        if (hour != other.hour) return false
        if (minute != other.minute) return false
        if (label != other.label) return false
        if (enabled != other.enabled) return false
        if (recurring != other.recurring) return false
        if (monday != other.monday) return false
        if (tuesday != other.tuesday) return false
        if (wednesday != other.wednesday) return false
        if (thursday != other.thursday) return false
        if (friday != other.friday) return false
        if (saturday != other.saturday) return false
        if (sunday != other.sunday) return false
        if (vibrate != other.vibrate) return false
        if (fadeIn != other.fadeIn) return false
        if (fadeInDuration != other.fadeInDuration) return false
        if (metadata != other.metadata) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + hour
        result = 31 * result + minute
        result = 31 * result + label.hashCode()
        result = 31 * result + enabled.hashCode()
        result = 31 * result + recurring.hashCode()
        result = 31 * result + monday.hashCode()
        result = 31 * result + tuesday.hashCode()
        result = 31 * result + wednesday.hashCode()
        result = 31 * result + thursday.hashCode()
        result = 31 * result + friday.hashCode()
        result = 31 * result + saturday.hashCode()
        result = 31 * result + sunday.hashCode()
        result = 31 * result + vibrate.hashCode()
        result = 31 * result + fadeIn.hashCode()
        result = 31 * result + fadeInDuration.hashCode()
        result = 31 * result + metadata.hashCode()
        return result
    }
}