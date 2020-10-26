package com.pghaz.revery.model.app.spotify

import android.os.Parcelable
import com.pghaz.revery.model.app.alarm.AlarmMetadata
import com.pghaz.revery.model.app.alarm.MediaType
import io.github.kaaes.spotify.webapi.core.models.Track
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TrackWrapper(
    val track: Track,
    override var shuffle: Boolean = false,
    override var shouldKeepPlaying: Boolean = false
) : BaseSpotifyMediaModel(shuffle, shouldKeepPlaying), Parcelable {

    fun toAlarmMetadata(): AlarmMetadata {
        return AlarmMetadata(
            uri = this.track.uri,
            href = this.track.href,
            type = MediaType.SPOTIFY_TRACK,
            name = this.track.name,
            description = ArtistWrapper.getArtistNames(this.track.artists),
            imageUrl = this.track.album.images[0].url,
            shuffle = this.shuffle,
            shouldKeepPlaying = this.shouldKeepPlaying
        )
    }
}