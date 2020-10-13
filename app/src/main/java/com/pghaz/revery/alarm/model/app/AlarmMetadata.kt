package com.pghaz.revery.alarm.model.app

import android.os.Parcel
import android.os.Parcelable
import com.pghaz.revery.alarm.model.room.RAlarmMetadata
import com.pghaz.revery.alarm.model.room.RAlarmType

class AlarmMetadata(
    var type: RAlarmType = RAlarmType.DEFAULT,
    var name: String? = null,
    var uri: String? = null,
    var description: String? = null,
    var imageUrl: String? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        RAlarmType.values()[parcel.readInt()],
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(type.ordinal)
        parcel.writeString(name)
        parcel.writeString(uri)
        parcel.writeString(description)
        parcel.writeString(imageUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AlarmMetadata> {
        override fun createFromParcel(parcel: Parcel): AlarmMetadata {
            return AlarmMetadata(parcel)
        }

        override fun newArray(size: Int): Array<AlarmMetadata?> {
            return arrayOfNulls(size)
        }

        fun fromDatabaseModel(metadata: RAlarmMetadata): AlarmMetadata {
            return AlarmMetadata(
                metadata.type,
                metadata.name,
                metadata.uri,
                metadata.description,
                metadata.imageUrl
            )
        }

        fun toDatabaseModel(metadata: AlarmMetadata?): RAlarmMetadata {
            return RAlarmMetadata(
                metadata?.type ?: RAlarmType.DEFAULT,
                metadata?.name,
                metadata?.uri,
                metadata?.description,
                metadata?.imageUrl
            )
        }
    }
}