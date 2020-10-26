package com.pghaz.revery.model.app

import android.os.Parcelable

abstract class BaseModel : Parcelable {
    companion object {
        const val NO_ID: Long = 0
    }
}