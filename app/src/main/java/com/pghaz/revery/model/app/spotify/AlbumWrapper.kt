package com.pghaz.revery.model.app.spotify

import android.os.Parcelable
import com.pghaz.revery.model.app.Alarm
import com.pghaz.revery.model.app.AlarmMetadata
import com.pghaz.revery.model.app.MediaType
import io.github.kaaes.spotify.webapi.core.models.Album
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AlbumWrapper(val album: Album) : BaseSpotifyMediaModel(), Parcelable {

    fun toAlarmMetadata(alarm: Alarm): AlarmMetadata {
        return AlarmMetadata(
            uri = this.album.uri,
            href = this.album.href,
            type = MediaType.SPOTIFY_ALBUM,
            name = this.album.name,
            description = ArtistWrapper.getArtistNames(this.album.artists),
            imageUrl = this.album.images[0].url,
            shuffle = alarm.metadata.shuffle,
            shouldKeepPlaying = alarm.metadata.shouldKeepPlaying,
            repeat = alarm.metadata.repeat,
        )
    }
}