package com.pghaz.revery.alarm.model.app

import android.os.Parcelable
import com.pghaz.revery.alarm.model.BaseModel
import com.pghaz.revery.alarm.model.room.RAlarmMetadata
import com.pghaz.revery.alarm.model.room.RAlarmType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AlarmMetadata(
    var alarmType: AlarmType = AlarmType.DEFAULT,
    var metadataId: String? = null,
    var uri: String? = null,
    var href: String? = null,
    var type: String? = null,
    var name: String? = null,
    var description: String? = null,
    var imageUrl: String? = null
) : BaseModel(), Parcelable {

    constructor(alarmMetadata: AlarmMetadata) : this(
        alarmMetadata.alarmType,
        alarmMetadata.metadataId,
        alarmMetadata.uri,
        alarmMetadata.href,
        alarmMetadata.type,
        alarmMetadata.name,
        alarmMetadata.description,
        alarmMetadata.imageUrl
    )

    companion object {
        fun fromDatabaseModel(metadata: RAlarmMetadata): AlarmMetadata {
            return AlarmMetadata(
                AlarmType.values()[metadata.alarmType.ordinal],
                metadataId = metadata.metadataId,
                uri = metadata.uri,
                href = metadata.href,
                type = metadata.type,
                name = metadata.name,
                description = metadata.description,
                imageUrl = metadata.imageUrl
            )
        }

        fun toDatabaseModel(metadata: AlarmMetadata): RAlarmMetadata {
            return RAlarmMetadata(
                RAlarmType.values()[metadata.alarmType.ordinal],
                metadataId = metadata.metadataId,
                uri = metadata.uri,
                href = metadata.href,
                type = metadata.type,
                name = metadata.name,
                description = metadata.description,
                imageUrl = metadata.imageUrl
            )
        }
    }
}