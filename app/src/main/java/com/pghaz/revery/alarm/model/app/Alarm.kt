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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Alarm) return false
        if (!super.equals(other)) return false

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
        if (uri != other.uri) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + id.hashCode()
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
        result = 31 * result + (uri?.hashCode() ?: 0)
        return result
    }
}