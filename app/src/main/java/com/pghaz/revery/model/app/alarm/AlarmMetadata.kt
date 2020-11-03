package com.pghaz.revery.model.app.alarm

import android.os.Parcelable
import com.pghaz.revery.model.app.BaseModel
import com.pghaz.revery.model.room.RAlarmMetadata
import com.pghaz.revery.model.room.RMediaType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AlarmMetadata(
    var uri: String? = null,
    var href: String? = null,
    var type: MediaType = MediaType.DEFAULT,
    var name: String? = null,
    var description: String? = null,
    var imageUrl: String? = null,
    var shuffle: Boolean = false,
    var shouldKeepPlaying: Boolean = false,
    var repeat: Int = 0
) : BaseModel(), Parcelable {

    constructor(metadata: AlarmMetadata) : this(
        metadata.uri,
        metadata.href,
        metadata.type,
        metadata.name,
        metadata.description,
        metadata.imageUrl,
        metadata.shuffle,
        metadata.shouldKeepPlaying,
        metadata.repeat
    )

    companion object {
        fun fromDatabaseModel(metadata: RAlarmMetadata): AlarmMetadata {
            return AlarmMetadata(
                uri = metadata.uri,
                href = metadata.href,
                type = MediaType.values()[metadata.type.ordinal],
                name = metadata.name,
                description = metadata.description,
                imageUrl = metadata.imageUrl,
                shuffle = metadata.shuffle,
                shouldKeepPlaying = metadata.shouldKeepPlaying,
                repeat = metadata.repeat
            )
        }

        fun toDatabaseModel(metadata: AlarmMetadata): RAlarmMetadata {
            return RAlarmMetadata(
                uri = metadata.uri,
                href = metadata.href,
                type = RMediaType.values()[metadata.type.ordinal],
                name = metadata.name,
                description = metadata.description,
                imageUrl = metadata.imageUrl,
                shuffle = metadata.shuffle,
                shouldKeepPlaying = metadata.shouldKeepPlaying,
                repeat = metadata.repeat
            )
        }
    }
}