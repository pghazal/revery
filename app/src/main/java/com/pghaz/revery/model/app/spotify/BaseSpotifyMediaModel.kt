package com.pghaz.revery.model.app.spotify

import android.os.Parcelable
import com.pghaz.revery.model.app.BaseModel

abstract class BaseSpotifyMediaModel(
    open var shuffle: Boolean,
    open var shouldKeepPlaying: Boolean
) : BaseModel(), Parcelable