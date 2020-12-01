package com.pghaz.revery.player

import android.content.Context
import android.media.AudioManager
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.VolumeProviderCompat

class VolumeKeyController(private val mContext: Context, private val streamType: Int) {

    interface VolumeKeyCallback {
        fun onVolumeKeyPressed()
    }

    private var volumeKeyCallback: VolumeKeyCallback? = null
    private var mMediaSession: MediaSessionCompat? = null

    private fun createMediaSession() {
        mMediaSession = MediaSessionCompat(mContext, "VolumeKeyController")
        mMediaSession!!.setFlags(
            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        )
        mMediaSession!!.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, 0, 0f)
                .build()
        )

        mMediaSession!!.setPlaybackToRemote(createVolumeProvider())
        mMediaSession!!.isActive = true
    }

    private fun createVolumeProvider(): VolumeProviderCompat {
        val audio = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentVolume = audio.getStreamVolume(streamType)
        val maxVolume = audio.getStreamMaxVolume(streamType)

        return object : VolumeProviderCompat(VOLUME_CONTROL_RELATIVE, maxVolume, currentVolume) {
            override fun onAdjustVolume(direction: Int) {
                super.onAdjustVolume(direction)
                volumeKeyCallback?.onVolumeKeyPressed()

                // Immediately destroy once key event is fired in order to avoid overriding
                // device volume up/down pressed
                release()
            }
        }
    }

    // Call when control needed, add a call to constructor if needed immediately
    fun setActive(active: Boolean) {
        if (mMediaSession != null) {
            mMediaSession!!.isActive = active
            return
        }
        createMediaSession()
    }

    // Call from Service's onDestroy method
    fun release() {
        if (mMediaSession != null && mMediaSession?.isActive == true) {
            mMediaSession?.release()
            mMediaSession?.isActive = false
            volumeKeyCallback = null
        }
    }

    fun setCallback(callback: VolumeKeyCallback) {
        volumeKeyCallback = callback
    }
}