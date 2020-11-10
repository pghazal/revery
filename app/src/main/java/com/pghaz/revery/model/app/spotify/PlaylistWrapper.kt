package com.pghaz.revery.model.app.spotify

import android.os.Parcelable
import com.pghaz.revery.model.app.MediaMetadata
import com.pghaz.revery.model.app.MediaType
import com.spotify.protocol.types.Repeat
import io.github.kaaes.spotify.webapi.core.models.PlaylistSimple
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PlaylistWrapper(val playlistSimple: PlaylistSimple) : BaseSpotifyMediaModel(),
    Parcelable {

    fun toAlarmMetadata(): MediaMetadata {
        return MediaMetadata(
            uri = this.playlistSimple.uri,
            href = this.playlistSimple.href,
            type = MediaType.SPOTIFY_PLAYLIST,
            name = this.playlistSimple.name,
            description = this.playlistSimple.description,
            imageUrl = this.playlistSimple.images[0].url,
            shuffle = false,
            shouldKeepPlaying = false,
            repeat = Repeat.OFF
        )
    }
}