package com.pghaz.revery.player

import android.animation.ValueAnimator
import android.content.Context
import android.media.AudioManager
import android.view.animation.LinearInterpolator
import androidx.annotation.CallSuper

abstract class AbstractPlayer(
    protected val context: Context,
    protected val streamType: Int,
    private val shouldUseDeviceVolume: Boolean
) {
    interface PlayerListener {
        fun onPlayerInitialized()

        fun onPlayerError(error: PlayerError)
    }

    protected var currentUri: String? = null
    protected var playerListener: PlayerListener? = null

    protected val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val initialDeviceVolume = audioManager.getStreamVolume(streamType)
    private val minVolume = audioManager.getStreamMinVolume(streamType)
    private val maxVolume = audioManager.getStreamMaxVolume(streamType)
    private var volumeAnimator: ValueAnimator? = null

    var fadeIn: Boolean = false
    var fadeInDuration: Long = 0

    @CallSuper
    open fun init(playerListener: PlayerListener?) {
        this.playerListener = playerListener
    }

    abstract fun prepareAsync(uri: String?)

    abstract fun prepare(uri: String?)

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
            } catch (error: Exception) {
                it.cancel()
                playerListener?.onPlayerError(PlayerError.FadeIn(error))
            }
        }

        volumeAnimator?.start()
    }

    override fun toString(): String {
        return "AbstractPlayer(streamType=$streamType," +
                " shouldUseDeviceVolume=$shouldUseDeviceVolume," +
                " currentUri=$currentUri, playerListener=$playerListener," +
                " initialDeviceVolume=$initialDeviceVolume," +
                " minVolume=$minVolume, maxVolume=$maxVolume," +
                " fadeIn=$fadeIn, fadeInDuration=$fadeInDuration)"
    }
}