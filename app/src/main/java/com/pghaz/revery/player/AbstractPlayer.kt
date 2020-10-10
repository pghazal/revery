package com.pghaz.revery.player

import android.content.Context

abstract class AbstractPlayer {
    enum class Type {
        DEFAULT,
        SPOTIFY
    }

    interface OnPlayerInitializedListener {
        fun onPlayerInitialized()
    }

    var onPlayerInitializedListener: OnPlayerInitializedListener? = null

    abstract fun getType(): Type

    abstract fun init(context: Context)

    abstract fun prepare(context: Context)

    abstract fun play()

    abstract fun pause()

    abstract fun release()
}