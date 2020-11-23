package com.pghaz.revery.model.app

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StandByEnabler(
    var hour: Int = 0,
    var minute: Int = 0,
    var enabled: Boolean = true
) : BaseModel(), Parcelable