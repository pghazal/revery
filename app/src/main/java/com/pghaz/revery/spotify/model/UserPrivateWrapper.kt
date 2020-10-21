package com.pghaz.revery.spotify.model

import android.os.Parcelable
import com.pghaz.revery.alarm.model.BaseModel
import io.github.kaaes.spotify.webapi.core.models.UserPrivate
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserPrivateWrapper(val userPrivate: UserPrivate) : BaseModel(), Parcelable