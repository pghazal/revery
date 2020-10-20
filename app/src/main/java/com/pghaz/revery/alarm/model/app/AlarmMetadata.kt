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
    var imageUrl: String? = null,
    var shuffle: Boolean = false,
    var shouldKeepPlaying: Boolean = false
) : BaseModel(), Parcelable {

    constructor(metadata: AlarmMetadata) : this(
        metadata.alarmType,
        metadata.metadataId,
        metadata.uri,
        metadata.href,
        metadata.type,
        metadata.name,
        metadata.description,
        metadata.imageUrl,
        metadata.shuffle,
        metadata.shouldKeepPlaying
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
                imageUrl = metadata.imageUrl,
                shuffle = metadata.shuffle,
                shouldKeepPlaying = metadata.shouldKeepPlaying
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
                imageUrl = metadata.imageUrl,
                shuffle = metadata.shuffle,
                shouldKeepPlaying = metadata.shouldKeepPlaying
            )
        }
    }
}