package com.pghaz.revery.model.app.spotify

import android.os.Parcelable
import com.pghaz.revery.model.app.Alarm
import com.pghaz.revery.model.app.MediaMetadata
import com.pghaz.revery.model.app.MediaType
import io.github.kaaes.spotify.webapi.core.models.Track
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TrackWrapper(val track: Track) : BaseSpotifyMediaModel(), Parcelable {

    fun toAlarmMetadata(alarm: Alarm): MediaMetadata {
        return MediaMetadata(
            uri = this.track.uri,
            href = this.track.href,
            type = MediaType.SPOTIFY_TRACK,
            name = this.track.name,
            description = ArtistWrapper.getArtistNames(this.track.artists),
            imageUrl = this.track.album.images[0].url,
            shuffle = alarm.metadata.shuffle,
            shouldKeepPlaying = alarm.metadata.shouldKeepPlaying,
            repeat = alarm.metadata.repeat,
        )
    }
}