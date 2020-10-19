package com.pghaz.revery.alarm.model

import android.os.Parcelable

abstract class BaseModel : Parcelable {
    companion object {
        const val NO_ID: Long = 0
    }
}