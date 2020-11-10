package com.pghaz.revery.model.app

import android.os.Parcelable
import com.pghaz.revery.model.room.RMediaMetadata
import com.pghaz.revery.model.room.RMediaType
import com.spotify.protocol.types.Repeat
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MediaMetadata(
    var uri: String? = null,
    var href: String? = null,
    var type: MediaType = MediaType.DEFAULT,
    var name: String? = null,
    var description: String? = null,
    var imageUrl: String? = null,
    var shuffle: Boolean = false,
    var shouldKeepPlaying: Boolean = false,
    var repeat: Int = Repeat.OFF
) : BaseModel(), Parcelable {

    constructor(metadata: MediaMetadata) : this(
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
        fun fromDatabaseModel(metadata: RMediaMetadata): MediaMetadata {
            return MediaMetadata(
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

        fun toDatabaseModel(metadata: MediaMetadata): RMediaMetadata {
            return RMediaMetadata(
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