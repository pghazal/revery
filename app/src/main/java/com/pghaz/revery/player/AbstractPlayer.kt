package com.pghaz.revery.player

import android.animation.ValueAnimator
import android.content.Context
import android.media.AudioManager
import android.view.animation.LinearInterpolator

abstract class AbstractPlayer(
    val audioManager: AudioManager,
    protected val streamType: Int,
    private val shouldUseDeviceVolume: Boolean
) {
    interface OnPlayerInitializedListener {
        fun onPlayerInitialized()
    }

    var onPlayerInitializedListener: OnPlayerInitializedListener? = null

    private var volumeAnimator: ValueAnimator? = null
    private val initialDeviceVolume = audioManager.getStreamVolume(streamType)
    private val minVolume = audioManager.getStreamMinVolume(streamType)
    private val maxVolume = audioManager.getStreamMaxVolume(streamType)

    var fadeIn: Boolean = false
    var fadeInDuration: Long = 0

    abstract fun init(context: Context)

    abstract fun prepare(context: Context, uri: String)

    abstract fun play()

    abstract fun pause()

    abstract fun release()

    protected fun initFadeIn() {
        audioManager.setStreamVolume(streamType, minVolume, 0)
    }

    protected fun resetVolumeFromFadeIn() {
        volumeAnimator?.cancel()
        volumeAnimator = null

        // Reset user volume
        audioManager.setStreamVolume(streamType, initialDeviceVolume, 0)
    }

    protected fun fadeIn() {
        volumeAnimator =
            ValueAnimator.ofInt(
                minVolume,
                if (shouldUseDeviceVolume) initialDeviceVolume else maxVolume
            )
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