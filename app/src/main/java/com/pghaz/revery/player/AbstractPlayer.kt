package com.pghaz.revery.player

import android.animation.ValueAnimator
import android.content.Context
import android.media.AudioManager
import android.view.animation.LinearInterpolator

abstract class AbstractPlayer(val audioManager: AudioManager, protected val streamType: Int) {
    interface OnPlayerInitializedListener {
        fun onPlayerInitialized()
    }

    var onPlayerInitializedListener: OnPlayerInitializedListener? = null

    private var volumeAnimator: ValueAnimator? = null
    private val initialUserVolume = audioManager.getStreamVolume(streamType)
    var fadeIn: Boolean = false
    var fadeInDuration: Long = 0

    abstract fun init(context: Context)

    abstract fun prepare(context: Context, uri: String)

    abstract fun play()

    abstract fun pause()

    abstract fun release()

    protected fun initFadeIn() {
        audioManager.setStreamVolume(streamType, 0, 0)
    }

    protected fun resetVolumeFromFadeIn() {
        volumeAnimator?.cancel()
        volumeAnimator = null

        // Reset user volume
        audioManager.setStreamVolume(streamType, initialUserVolume, 0)
    }

    protected fun fadeIn() {
        val initialVolume = audioManager.getStreamMinVolume(streamType)
        val maxVolume = audioManager.getStreamMaxVolume(streamType)

        volumeAnimator = ValueAnimator.ofInt(initialVolume, maxVolume)
        volumeAnimator?.interpolator = LinearInterpolator()
        volumeAnimator?.duration = fadeInDuration * 1000

        volumeAnimator?.addUpdateListener {
            val volume = it.animatedValue as Int
            try {
                audioManager.setStreamVolume(streamType, volume, 0)
            } catch (e: Exception) {
                it.cancel()
                throw e // rethrow for now
            }
        }

        volumeAnimator?.start()
    }
}