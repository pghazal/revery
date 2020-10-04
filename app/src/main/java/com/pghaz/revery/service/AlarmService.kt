package com.pghaz.revery.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.*
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.pghaz.revery.R
import com.pghaz.revery.alarm.AlarmHandler
import com.pghaz.revery.alarm.RingActivity
import com.pghaz.revery.application.ReveryApplication
import com.pghaz.revery.repository.Alarm
import com.pghaz.revery.repository.AlarmRepository


// TODO: make service with View so that we can show on lock screen
class AlarmService : LifecycleService() {

    companion object {
        private const val TAG = "AlarmService"

        private const val AUDIO_FOCUS_PARAM = AudioManager.AUDIOFOCUS_GAIN
    }

    private lateinit var alarmHandler: AlarmHandler
    private lateinit var alarmRepository: AlarmRepository

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var vibrator: Vibrator

    private lateinit var audioManager: AudioManager
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

    override fun onCreate() {
        super.onCreate()

        alarmHandler = AlarmHandler()
        alarmRepository = AlarmRepository(application)

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        initMediaPlayer()
    }

    private fun initMediaPlayer() {
        val alarmToneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(this, alarmToneUri)
        mediaPlayer.isLooping = true
        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )

        mediaPlayer.setOnPreparedListener {
            it.start() // Step 3: when focused and prepared, play audio
        }
    }

    override fun onStartCommand(alarmIntent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(alarmIntent, flags, startId)

        alarmIntent?.let {
            disableOneShotAlarm(it)

            requestAudioFocus() // Step 1: request focus

            if (it.getBooleanExtra(Alarm.VIBRATE, false)) {
                vibrate()
            }

            val notification = buildAlarmNotification(it)
            startForeground(1, notification)
        }

        return START_STICKY
    }

    private fun requestAudioFocus() {
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
            mediaPlayer.prepareAsync() // Step 2: prepare audio async
        } else {
            Log.e(TAG, "# Request audio focus failed")
        }
    }

    private fun abandonAudioFocus() {
        val result = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            audioManager.abandonAudioFocus(onAudioFocusChangeListener)
        }
    }

    private fun disableOneShotAlarm(alarmIntent: Intent) {
        if (!alarmIntent.getBooleanExtra(Alarm.RECURRING, false)) {
            val alarmId = alarmIntent.getLongExtra(Alarm.ID, 0)
            alarmRepository.get(alarmId).observe(this, { alarm ->
                alarm?.let {
                    alarmHandler.disableAlarm(it)
                    alarmRepository.update(it)
                }
            })
        }
    }

    private fun buildAlarmNotification(alarmIntent: Intent): Notification {
        val notificationIntent = Intent(this, RingActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val alarmTitle = String.format("%s", alarmIntent.getStringExtra(Alarm.LABEL))

        // TODO: custom notification
        return NotificationCompat.Builder(this, ReveryApplication.CHANNEL_ID)
            .setContentTitle(alarmTitle)
            .setContentText("Ring Ring...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun vibrate() {
        // Vibrate for 1000 milliseconds
        // Sleep for 1000 milliseconds
        val pattern = longArrayOf(1000, 1000)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            vibrator.vibrate(pattern, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        vibrator.cancel()
        abandonAudioFocus()
    }
}
