package com.pghaz.revery.alarm.model.app

import android.os.Parcel
import android.os.Parcelable
import com.pghaz.revery.alarm.model.room.RAlarm

data class Alarm(
    var id: Long = NO_ID,
    var hour: Int = 0,
    var minute: Int = 0,
    var label: String = "",
    var recurring: Boolean = false,
    var enabled: Boolean = true,
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
    var metadata: AlarmMetadata? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readLong(),
        parcel.readParcelable(AlarmMetadata::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeInt(hour)
        parcel.writeInt(minute)
        parcel.writeString(label)
        parcel.writeByte(if (recurring) 1 else 0)
        parcel.writeByte(if (enabled) 1 else 0)
        parcel.writeByte(if (monday) 1 else 0)
        parcel.writeByte(if (tuesday) 1 else 0)
        parcel.writeByte(if (wednesday) 1 else 0)
        parcel.writeByte(if (thursday) 1 else 0)
        parcel.writeByte(if (friday) 1 else 0)
        parcel.writeByte(if (saturday) 1 else 0)
        parcel.writeByte(if (sunday) 1 else 0)
        parcel.writeByte(if (vibrate) 1 else 0)
        parcel.writeByte(if (fadeIn) 1 else 0)
        parcel.writeLong(fadeInDuration)
        parcel.writeParcelable(metadata, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Alarm> {
        override fun createFromParcel(parcel: Parcel): Alarm {
            return Alarm(parcel)
        }

        override fun newArray(size: Int): Array<Alarm?> {
            return arrayOfNulls(size)
        }

        fun fromDatabaseModel(alarm: RAlarm): Alarm {
            val metadata = AlarmMetadata.fromDatabaseModel(alarm.metadata)

            return Alarm(
                alarm.id, alarm.hour, alarm.minute, alarm.label, alarm.recurring,
                alarm.enabled, alarm.monday, alarm.tuesday, alarm.wednesday, alarm.thursday,
                alarm.friday, alarm.saturday, alarm.sunday, alarm.vibrate, alarm.fadeIn,
                alarm.fadeInDuration, metadata
            )
        }

        fun toDatabaseModel(alarm: Alarm): RAlarm {
            val metadata = AlarmMetadata.toDatabaseModel(alarm.metadata)

            return RAlarm(
                alarm.id, alarm.hour, alarm.minute, alarm.label, alarm.recurring, alarm.enabled,
                alarm.monday, alarm.tuesday, alarm.wednesday, alarm.thursday, alarm.friday,
                alarm.saturday, alarm.sunday, alarm.vibrate, alarm.fadeIn, alarm.fadeInDuration,
                metadata
            )
        }

        const val NO_ID: Long = 0
    }
}