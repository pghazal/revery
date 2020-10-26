package com.pghaz.revery.model.app.spotify

import android.os.Parcelable
import com.pghaz.revery.model.app.alarm.AlarmMetadata
import com.pghaz.revery.model.app.alarm.MediaType
import io.github.kaaes.spotify.webapi.core.models.Album
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AlbumWrapper(
    val album: Album,
    override var shuffle: Boolean = false,
    override var shouldKeepPlaying: Boolean = false
) : BaseSpotifyMediaModel(shuffle, shouldKeepPlaying), Parcelable {

    fun toAlarmMetadata(): AlarmMetadata {
        return AlarmMetadata(
            uri = this.album.uri,
            href = this.album.href,
            type = MediaType.SPOTIFY_ALBUM,
            name = this.album.name,
            description = ArtistWrapper.getArtistNames(this.album.artists),
            imageUrl = this.album.images[0].url,
            shuffle = this.shuffle,
            shouldKeepPlaying = this.shouldKeepPlaying
        )
    }
}