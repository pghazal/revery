package com.pghaz.revery.repository.converter

import androidx.room.TypeConverter
import com.pghaz.revery.model.room.RMediaType

class MediaTypeConverters {

    @TypeConverter
    fun deserializeMediaType(value: Int): RMediaType {
        return RMediaType.values()[value]
    }

    @TypeConverter
    fun serializeMediaType(value: RMediaType): Int {
        return value.ordinal
    }

    /*@TypeConverter
    fun deserializeMapString(value: String): Map<String, String> {
        val type = object : TypeToken<Map<String, String>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun serializeMapString(map: Map<String, String>): String {
        return Gson().toJson(map)
    }

    @TypeConverter
    fun deserializeListString(value: String): List<String> {
        val type = object : TypeToken<ArrayList<String>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun serializeListString(list: List<String>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun deserializeListImage(value: String): List<Image> {
        val type = object : TypeToken<ArrayList<Image>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun serializeListImage(list: List<Image>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun deserializeListArtistSimple(value: String): List<ArtistSimple> {
        val type = object : TypeToken<ArrayList<ArtistSimple>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun serializeListArtistSimple(list: List<ArtistSimple>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun deserializeListCopyright(value: String): List<Copyright> {
        val type = object : TypeToken<ArrayList<Copyright>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun serializeListCopyright(list: List<Copyright>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun deserializePagerTrackSimple(value: String): Pager<TrackSimple> {
        val type = object : TypeToken<Pager<TrackSimple>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun serializePagerTrackSimple(pager: Pager<TrackSimple>): String {
        return Gson().toJson(pager)
    }

    @TypeConverter
    fun deserializeFollowers(value: String): Followers {
        val type = object : TypeToken<Followers>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun serializeFollowers(followers: Followers): String {
        return Gson().toJson(followers)
    }

    @TypeConverter
    fun deserializeUserPublic(value: String): UserPublic {
        val type = object : TypeToken<UserPublic>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun serializeUserPublic(user: UserPublic): String {
        return Gson().toJson(user)
    }

    @TypeConverter
    fun deserializePlaylistTracksInformation(value: String): PlaylistTracksInformation {
        val type = object : TypeToken<PlaylistTracksInformation>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun serializePlaylistTracksInformation(tracksInformation: PlaylistTracksInformation): String {
        return Gson().toJson(tracksInformation)
    }

    @TypeConverter
    fun deserializeLinkedTrack(value: String): LinkedTrack {
        val type = object : TypeToken<LinkedTrack>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun serializeLinkedTrack(linkedTrack: LinkedTrack): String {
        return Gson().toJson(linkedTrack)
    }

    @TypeConverter
    fun deserializeAlbumSimple(value: String): AlbumSimple {
        val type = object : TypeToken<AlbumSimple>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun serializeAlbumSimple(album: AlbumSimple): String {
        return Gson().toJson(album)
    }*/
}