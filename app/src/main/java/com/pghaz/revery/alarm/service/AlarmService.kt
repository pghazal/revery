package com.pghaz.revery.alarm.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Binder
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.pghaz.revery.MainActivity
import com.pghaz.revery.R
import com.pghaz.revery.alarm.AlarmHandler
import com.pghaz.revery.alarm.model.app.Alarm
import com.pghaz.revery.alarm.model.app.AlarmMetadata
import com.pghaz.revery.alarm.model.room.RAlarmType
import com.pghaz.revery.alarm.repository.AlarmRepository
import com.pghaz.revery.application.ReveryApplication
import com.pghaz.revery.player.AbstractPlayer
import com.pghaz.revery.player.DefaultPlayer
import com.pghaz.revery.player.SpotifyPlayer

class AlarmService : LifecycleService(), AbstractPlayer.OnPlayerInitializedListener {

    companion object {
        private const val TAG = "AlarmService"

        var isRunning: Boolean = false // this is ugly: find a way to check if service is alive
    }

    private lateinit var alarmHandler: AlarmHandler
    private lateinit var alarmRepository: AlarmRepository

    private lateinit var vibrator: Vibrator
    private lateinit var notification: Notification

    private lateinit var audioManager: AudioManager

    private lateinit var player: AbstractPlayer

    private val mBinder = AlarmServiceBinder()

    inner class AlarmServiceBinder : Binder() {
        fun getService(): AbstractPlayer? {
            return player
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.e(TAG, "onUnbind")
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true

        alarmHandler = AlarmHandler()
        alarmRepository = AlarmRepository(application)

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onStartCommand(alarmIntent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(alarmIntent, flags, startId)

        alarmIntent?.let {
            val alarmId = it.getLongExtra(Alarm.ID, 0)
            val recurring = it.getBooleanExtra(Alarm.RECURRING, false)
            val alarmLabel = it.getStringExtra(Alarm.LABEL)
            val alarmVibrate = it.getBooleanExtra(Alarm.VIBRATE, false)
            var alarmMetadata = it.getParcelableExtra(Alarm.METADATA) as AlarmMetadata?
            alarmMetadata = safeInitMetadataIfNeeded(alarmMetadata)

            notification = buildAlarmNotification(alarmId, alarmLabel)
            disableOneShotAlarm(recurring, alarmId)

            player = when (alarmMetadata.type) {
                RAlarmType.DEFAULT -> {
                    DefaultPlayer(audioManager)
                }

                RAlarmType.SPOTIFY -> {
                    SpotifyPlayer()
                }
            }

            player.onPlayerInitializedListener = this

            when (alarmMetadata.type) {
                RAlarmType.DEFAULT -> {
                    (player as DefaultPlayer).init(this)
                    (player as DefaultPlayer).prepare(this, alarmMetadata.uri!!)
                }

                RAlarmType.SPOTIFY -> {
                    (player as SpotifyPlayer).init(this)
                    (player as SpotifyPlayer).prepare(this, alarmMetadata.uri!!)
                }
            }

            if (alarmVibrate) {
                vibrate()
            }
        }

        return START_STICKY
    }

    private fun safeInitMetadataIfNeeded(nullableMetadata: AlarmMetadata?): AlarmMetadata {
        if (nullableMetadata?.uri != null) {
            return nullableMetadata
        }

        val nonNullMetadata = AlarmMetadata()
        // default ringtone
        nonNullMetadata.type = RAlarmType.DEFAULT
        nonNullMetadata.name = nullableMetadata?.name
        nonNullMetadata.uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString()
        nonNullMetadata.description = nullableMetadata?.description
        nonNullMetadata.imageUrl = nullableMetadata?.imageUrl

        return nonNullMetadata
    }

    override fun onPlayerInitialized() {
        startForeground(1, notification)

        player.play()
    }

    private fun disableOneShotAlarm(recurring: Boolean, alarmId: Long) {
        if (!recurring) {
            alarmRepository.get(alarmId).observe(this, { alarm ->
                alarm?.let {
                    alarmHandler.disableAlarm(it)
                    alarmRepository.update(it)
                }
            })
        }
    }

    /**
     * The MOST IMPORTANT thing to remember here in order to start the Activity in the Service is:
     *  - set a requestCode different than 0 in the PendingIntent.getActivity()
     *  - add .setFullScreenIntent(pendingIntent, true) when creating the notification
     */
    private fun buildAlarmNotification(alarmId: Long, alarmLabel: String?): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

        val pendingIntent = PendingIntent.getActivity(
            this,
            alarmId.toInt(),
            notificationIntent,
            0
        )

        // TODO: custom notification
        return NotificationCompat.Builder(this, ReveryApplication.CHANNEL_ID)
            .setContentTitle(String.format("%s", alarmLabel))
            .setContentText("Ring Ring...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setFullScreenIntent(pendingIntent, true)
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
        release()
        super.onDestroy()
        Log.e(TAG, "onDestroy()")
    }

    // TODO: what about user force-stop the app
    private fun release() {
        player.pause()
        player.release()

        vibrator.cancel()

        isRunning = false
    }
}
