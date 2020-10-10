package com.pghaz.revery.player

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log

class DefaultPlayer(private val audioManager: AudioManager) : AbstractPlayer() {

    private lateinit var mediaPlayer: MediaPlayer

    private val onAudioFocusChangeListener =
        AudioManager.OnAudioFocusChangeListener { focusChange ->
            // TODO: handle focus changed ??
        }

    // Used only for Android O and later
    private val audioFocusRequest =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            AudioFocusRequest.Builder(AUDIO_FOCUS_PARAM)
                .setOnAudioFocusChangeListener(onAudioFocusChangeListener)
                .build()
        } else {
            null
        }

    override fun init(context: Context) {
        mediaPlayer = MediaPlayer()
        mediaPlayer.isLooping = true
        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )

        mediaPlayer.setOnPreparedListener {
            onPlayerInitializedListener?.onPlayerInitialized()
        }
    }

    override fun prepare(context: Context, uri: String) {
        mediaPlayer.setDataSource(context, Uri.parse(uri))

        // Request audio focus for play back
        val result = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.requestAudioFocus(it) }
        } else {
            audioManager.requestAudioFocus(
                onAudioFocusChangeListener,
                AudioManager.STREAM_ALARM,
                AUDIO_FOCUS_PARAM
            )
        }

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mediaPlayer.prepareAsync()
        } else {
            Log.e(TAG, "# Request audio focus failed")
        }
    }

    override fun play() {
        mediaPlayer.start()
    }

    override fun pause() {
        mediaPlayer.pause()
    }

    private fun abandonAudioFocus() {
        val result = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            audioManager.abandonAudioFocus(onAudioFocusChangeListener)
        }
    }

    override fun release() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()

        abandonAudioFocus()
    }

    companion object {
        private const val TAG = "DefaultPlayer"

        private const val AUDIO_FOCUS_PARAM = AudioManager.AUDIOFOCUS_GAIN
    }
}