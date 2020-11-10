package com.pghaz.revery.model.app.spotify

import android.os.Parcelable
import com.pghaz.revery.model.app.MediaMetadata
import com.pghaz.revery.model.app.MediaType
import com.spotify.protocol.types.Repeat
import io.github.kaaes.spotify.webapi.core.models.Album
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AlbumWrapper(val album: Album) : BaseSpotifyMediaModel(), Parcelable {

    fun toMediaMetadata(): MediaMetadata {
        return MediaMetadata(
            uri = this.album.uri,
            href = this.album.href,
            type = MediaType.SPOTIFY_ALBUM,
            name = this.album.name,
            description = ArtistWrapper.getArtistNames(this.album.artists),
            imageUrl = this.album.images[0].url,
            shuffle = false,
            shouldKeepPlaying = false,
            repeat = Repeat.OFF
        )
    }
}