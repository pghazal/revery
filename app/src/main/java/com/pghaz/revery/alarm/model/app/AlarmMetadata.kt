package com.pghaz.revery.alarm.model.app

import android.os.Parcelable
import com.pghaz.revery.alarm.model.BaseModel
import com.pghaz.revery.alarm.model.room.RAlarmMetadata
import com.pghaz.revery.alarm.model.room.RAlarmType
import kotlinx.android.parcel.Parcelize

@Parcelize
class AlarmMetadata(
    var alarmType: AlarmType = AlarmType.DEFAULT,
    var metadataId: String? = null,
    var uri: String? = null,
    var href: String? = null,
    var type: String? = null,
    var name: String? = null,
    var description: String? = null,
    var imageUrl: String? = null
) : BaseModel(), Parcelable {

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AlarmMetadata) return false

        if (alarmType != other.alarmType) return false
        if (metadataId != other.metadataId) return false
        if (uri != other.uri) return false
        if (href != other.href) return false
        if (type != other.type) return false
        if (name != other.name) return false
        if (description != other.description) return false
        if (imageUrl != other.imageUrl) return false

        return true
    }

    override fun hashCode(): Int {
        var result = alarmType.hashCode()
        result = 31 * result + (metadataId?.hashCode() ?: 0)
        result = 31 * result + (uri?.hashCode() ?: 0)
        result = 31 * result + (href?.hashCode() ?: 0)
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (imageUrl?.hashCode() ?: 0)
        return result
    }
}