package com.pghaz.revery.player

import android.content.Context

abstract class AbstractPlayer {
    interface OnPlayerInitializedListener {
        fun onPlayerInitialized()
    }

    var onPlayerInitializedListener: OnPlayerInitializedListener? = null

    abstract fun init(context: Context)

    abstract fun prepare(context: Context, uri: String)

    abstract fun play()

    abstract fun pause()

    abstract fun release()
}