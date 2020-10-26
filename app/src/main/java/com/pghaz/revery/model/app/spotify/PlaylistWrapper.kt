package com.pghaz.revery.model.app.spotify

import android.os.Parcelable
import com.pghaz.revery.model.app.alarm.AlarmMetadata
import com.pghaz.revery.model.app.alarm.MediaType
import io.github.kaaes.spotify.webapi.core.models.PlaylistSimple
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PlaylistWrapper(
    val playlistSimple: PlaylistSimple,
    override var shuffle: Boolean = false,
    override var shouldKeepPlaying: Boolean = false
) : BaseSpotifyMediaModel(shuffle, shouldKeepPlaying), Parcelable {

    fun toAlarmMetadata(): AlarmMetadata {
        return AlarmMetadata(
            uri = this.playlistSimple.uri,
            href = this.playlistSimple.href,
            type = MediaType.SPOTIFY_PLAYLIST,
            name = this.playlistSimple.name,
            description = this.playlistSimple.description,
            imageUrl = this.playlistSimple.images[0].url,
            shuffle = this.shuffle,
            shouldKeepPlaying = this.shouldKeepPlaying
        )
    }
}