package com.pghaz.revery.spotify.model

import android.os.Parcelable
import com.pghaz.revery.alarm.model.BaseModel
import io.github.kaaes.spotify.webapi.core.models.Artist
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ArtistWrapper(val artist: Artist) : BaseModel(), Parcelable