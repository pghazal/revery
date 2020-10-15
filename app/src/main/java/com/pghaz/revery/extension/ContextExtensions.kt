package com.pghaz.revery.extension

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import com.pghaz.revery.BuildConfig

fun Context.toast(@StringRes resource: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, resource, duration).show()
}

fun Context.toast(text: String?, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text.orEmpty(), duration).show()
}

fun Context.logError(text: String) {
    if (BuildConfig.DEBUG) {
        Log.e(this.javaClass.simpleName, text)
    }
}

fun Context.logError(text: String?, throwable: Throwable?) {
    if (BuildConfig.DEBUG) {
        Log.e(this.javaClass.simpleName, text, throwable)
    }
}