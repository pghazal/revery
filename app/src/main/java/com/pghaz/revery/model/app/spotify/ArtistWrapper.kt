package com.pghaz.revery.model.app.spotify

import android.os.Parcelable
import com.pghaz.revery.model.app.alarm.AlarmMetadata
import com.pghaz.revery.model.app.alarm.MediaType
import io.github.kaaes.spotify.webapi.core.models.Artist
import io.github.kaaes.spotify.webapi.core.models.ArtistSimple
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ArtistWrapper(
    val artist: Artist,
    override var shuffle: Boolean = false,
    override var shouldKeepPlaying: Boolean = false
) : BaseSpotifyMediaModel(shuffle, shouldKeepPlaying), Parcelable {

    fun toAlarmMetadata(): AlarmMetadata {
        return AlarmMetadata(
            uri = this.artist.uri,
            href = this.artist.href,
            type = MediaType.SPOTIFY_ARTIST,
            name = this.artist.name,
            description = null,
            imageUrl = this.artist.images[0].url,
            shuffle = this.shuffle,
            shouldKeepPlaying = this.shouldKeepPlaying
        )
    }

    companion object {
        fun getArtistNames(artists: MutableList<ArtistSimple>): String {
            val stringBuilder = StringBuilder("")
            val iterator = artists.iterator()
            val artist = iterator.next()
            stringBuilder.append(artist.name)
            while (iterator.hasNext()) {
                val next = iterator.next()
                stringBuilder.append(", ")
                stringBuilder.append(next.name)
            }
            return stringBuilder.toString()
        }
    }
}