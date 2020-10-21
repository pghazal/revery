package com.pghaz.revery.spotify.model

import android.os.Parcelable
import com.pghaz.revery.alarm.model.BaseModel
import io.github.kaaes.spotify.webapi.core.models.Album
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AlbumWrapper(val album: Album) : BaseModel(), Parcelable