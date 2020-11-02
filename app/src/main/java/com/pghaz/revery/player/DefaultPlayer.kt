package com.pghaz.revery.player

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import com.pghaz.revery.extension.logError
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException


class DefaultPlayer(context: Context, shouldUseDeviceVolume: Boolean) :
    AbstractPlayer(context, AudioManager.STREAM_ALARM, shouldUseDeviceVolume) {

    private var mediaPlayer: MediaPlayer? = null

    private val onAudioFocusChangeListener = AudioManager.OnAudioFocusChangeListener {}

    // Used only for Android O and later
    private val audioFocusRequest =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            AudioFocusRequest.Builder(AUDIO_FOCUS_PARAM)
                .setOnAudioFocusChangeListener(onAudioFocusChangeListener)
                .build()
        } else {
            null
        }

    override fun init(playerListener: PlayerListener?) {
        super.init(playerListener)

        mediaPlayer = MediaPlayer()
        mediaPlayer!!.isLooping = true
        mediaPlayer!!.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(if (streamType == AudioManager.STREAM_ALARM) AudioAttributes.USAGE_ALARM else AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )

        mediaPlayer!!.setOnErrorListener { _, _, extras ->
            val throwable = Throwable(this.toString())

            val error = when (extras) {
                MediaPlayer.MEDIA_ERROR_IO -> PlayerError.DefaultPlayerIO(throwable)
                MediaPlayer.MEDIA_ERROR_MALFORMED -> PlayerError.DefaultPlayerMalformed(throwable)
                MediaPlayer.MEDIA_ERROR_UNSUPPORTED -> PlayerError.DefaultPlayerUnsupported(
                    throwable
                )
                MediaPlayer.MEDIA_ERROR_TIMED_OUT -> PlayerError.DefaultPlayerTimedOut(throwable)
                else -> PlayerError.DefaultPlayerUnknown(throwable)
            }

            playerListener?.onPlayerError(error)

            return@setOnErrorListener true
        }

        mediaPlayer!!.setOnPreparedListener {
            playerListener?.onPlayerInitialized(this)
        }
    }

    override fun prepareAsync(uri: String?) {
        setDataSource(uri)

        // Request audio focus for play back
        val result = requestAudioFocus()

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mediaPlayer?.prepareAsync()
        } else {
            context.logError("# Request audio focus failed")
        }
    }

    override fun prepare(uri: String?) {
        setDataSource(uri)

        // Request audio focus for play back
        val result = requestAudioFocus()

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mediaPlayer?.prepare()
        } else {
            context.logError("# Request audio focus failed")
        }
    }

    private fun setDataSource(stringUri: String?) {
        if (stringUri == null) {
            throw NullPointerException("setDataSource(): currentUri cannot be null")
        }

        currentUri = stringUri

        val uri: Uri = Uri.parse(currentUri)

        if ("content" == uri.scheme) {
            mediaPlayer?.setDataSource(context, uri)
        } else {
            val file = File(uri.path!!)

            if (!file.exists()) {
                throw FileNotFoundException("setDataSource(): file at currentUri not found")
            }

            val inputStream = FileInputStream(file)
            mediaPlayer?.setDataSource(inputStream.fd)
        }
    }

    private fun requestAudioFocus(): Int? {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.requestAudioFocus(it) }
        } else {
            audioManager.requestAudioFocus(
                onAudioFocusChangeListener,
                streamType,
                AUDIO_FOCUS_PARAM
            )
        }
    }

    override fun start() {
        if (fadeIn) {
            initFadeIn()
        } else {
            initVolume()
        }

        play()

        if (fadeIn) {
            fadeIn()
        }
    }

    override fun stop() {
        pause()

        resetInitialDeviceVolume()
    }

    override fun play() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
            }
        }
    }

    override fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
    }

    override fun skipNext() {
        // do nothing
    }

    override fun skipPrevious() {
        // do nothing
    }

    private fun abandonAudioFocus() {
        val result = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            audioManager.abandonAudioFocus(onAudioFocusChangeListener)
        }
    }

    override fun release() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.reset()
            it.release()
        }

        mediaPlayer = null

        abandonAudioFocus()
    }

    companion object {
        private const val AUDIO_FOCUS_PARAM = AudioManager.AUDIOFOCUS_GAIN
    }
}