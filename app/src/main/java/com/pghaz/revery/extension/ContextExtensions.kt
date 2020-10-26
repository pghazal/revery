package com.pghaz.revery.extension

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import com.pghaz.revery.BuildConfig

fun Context.toastDebug(@StringRes resource: Int, duration: Int = Toast.LENGTH_SHORT) {
    if (BuildConfig.DEBUG) {
        Toast.makeText(this, resource, duration).show()
    }
}

fun Context.toastDebug(text: String?, duration: Int = Toast.LENGTH_SHORT) {
    if (BuildConfig.DEBUG) {
        Toast.makeText(this, text.orEmpty(), duration).show()
    }
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

fun Context.logInfo(text: String) {
    if (BuildConfig.DEBUG) {
        Log.i(this.javaClass.simpleName, text)
    }
}

fun Context.logInfo(text: String?, throwable: Throwable?) {
    if (BuildConfig.DEBUG) {
        Log.i(this.javaClass.simpleName, text, throwable)
    }
}

fun Context.logDebug(text: String) {
    if (BuildConfig.DEBUG) {
        Log.d(this.javaClass.simpleName, text)
    }
}

fun Context.logDebug(text: String?, throwable: Throwable?) {
    if (BuildConfig.DEBUG) {
        Log.d(this.javaClass.simpleName, text, throwable)
    }
}

fun Context.logWarning(text: String) {
    if (BuildConfig.DEBUG) {
        Log.w(this.javaClass.simpleName, text)
    }
}

fun Context.logWarning(text: String?, throwable: Throwable?) {
    if (BuildConfig.DEBUG) {
        Log.w(this.javaClass.simpleName, text, throwable)
    }
}

fun Context.logVerbose(text: String) {
    if (BuildConfig.DEBUG) {
        Log.v(this.javaClass.simpleName, text)
    }
}

fun Context.logVerbose(text: String?, throwable: Throwable?) {
    if (BuildConfig.DEBUG) {
        Log.v(this.javaClass.simpleName, text, throwable)
    }
}