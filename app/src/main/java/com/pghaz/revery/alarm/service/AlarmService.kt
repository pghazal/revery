package com.pghaz.revery.alarm.service

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.RingtoneManager
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.pghaz.revery.MainActivity
import com.pghaz.revery.R
import com.pghaz.revery.alarm.AlarmHandler
import com.pghaz.revery.alarm.broadcastreceiver.AlarmBroadcastReceiver
import com.pghaz.revery.alarm.model.app.Alarm
import com.pghaz.revery.alarm.model.app.AlarmMetadata
import com.pghaz.revery.alarm.model.room.RAlarmType
import com.pghaz.revery.alarm.repository.AlarmRepository
import com.pghaz.revery.application.ReveryApplication
import com.pghaz.revery.extension.logError
import com.pghaz.revery.player.AbstractPlayer
import com.pghaz.revery.player.DefaultPlayer
import com.pghaz.revery.player.SpotifyPlayer
import com.pghaz.revery.settings.SettingsHandler
import com.pghaz.revery.util.IntentUtils
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class AlarmService : LifecycleService(), AbstractPlayer.OnPlayerInitializedListener {

    companion object {
        const val ACTION_ALARM_SERVICE_SHOULD_STOP =
            "com.pghaz.revery.ACTION_ALARM_SERVICE_SHOULD_STOP"

        fun getServiceShouldStopIntent(context: Context): Intent {
            val intent =
                Intent(context.applicationContext, AlarmServiceBroadcastReceiver::class.java)
            intent.action = ACTION_ALARM_SERVICE_SHOULD_STOP
            return intent
        }

        var isRunning: Boolean = false // this is ugly: find a way to check if service is alive
        var alarm: Alarm = Alarm() // this is very ugly: find a way to get the alarm
    }

    private lateinit var alarmRepository: AlarmRepository

    private lateinit var vibrator: Vibrator
    private lateinit var notification: Notification

    private lateinit var player: AbstractPlayer

    private var receiver = AlarmServiceBroadcastReceiver()

    inner class AlarmServiceBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                if (ACTION_ALARM_SERVICE_SHOULD_STOP == it.action) {
                    logError(ACTION_ALARM_SERVICE_SHOULD_STOP)
                    pausePlayerAndVibrator()
                    player.release()

                    stopSelf() // will call onDestroy()
                }
            }
        }
    }

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
        logError("onUnbind")
        return super.onUnbind(intent)
    }

    private fun registerToLocalAlarmBroadcastReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_ALARM_SERVICE_SHOULD_STOP)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter)
    }

    private fun unregisterFromLocalAlarmBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true

        alarmRepository = AlarmRepository(application)

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        registerToLocalAlarmBroadcastReceiver()
    }

    override fun onStartCommand(alarmIntent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(alarmIntent, flags, startId)

        alarmIntent?.let {
            alarm = IntentUtils.safeGetAlarmFromIntent(it)
            val fadeInDuration = SettingsHandler.getFadeInDuration(this)

            var alarmMetadata = alarm.metadata
            alarmMetadata = safeInitMetadataIfNeeded(alarmMetadata)

            notification = buildAlarmNotification(alarm)
            disableOneShotAlarm(alarm)

            val shouldUseDeviceVolume = SettingsHandler.getShouldUseDeviceVolume(this)

            player = if (!this::player.isInitialized) {
                getInitializedPlayer(alarmMetadata.type, shouldUseDeviceVolume)
            } else {
                pausePlayerAndVibrator()
                getInitializedPlayer(alarmMetadata.type, shouldUseDeviceVolume)
            }

            when (alarmMetadata.type) {
                RAlarmType.DEFAULT -> {
                    (player as DefaultPlayer).fadeIn = alarm.fadeIn
                    (player as DefaultPlayer).fadeInDuration = fadeInDuration
                    (player as DefaultPlayer).prepare(alarmMetadata.uri!!)
                }

                RAlarmType.SPOTIFY -> {
                    (player as SpotifyPlayer).fadeIn = alarm.fadeIn
                    (player as SpotifyPlayer).fadeInDuration = fadeInDuration
                    (player as SpotifyPlayer).prepare(alarmMetadata.uri!!)
                }
            }

            if (alarm.vibrate) {
                vibrate()
            }
        }

        return START_STICKY
    }

    private fun getInitializedPlayer(
        type: RAlarmType,
        shouldUseDeviceVolume: Boolean
    ): AbstractPlayer {
        return when (type) {
            RAlarmType.DEFAULT -> {
                DefaultPlayer(this, shouldUseDeviceVolume)
            }

            RAlarmType.SPOTIFY -> {
                SpotifyPlayer(this, shouldUseDeviceVolume)
            }
        }.apply {
            this.onPlayerInitializedListener = this@AlarmService

            when (type) {
                RAlarmType.DEFAULT -> {
                    (this as DefaultPlayer).init()
                }

                RAlarmType.SPOTIFY -> {
                    (this as SpotifyPlayer).init()
                }
            }
        }
    }

    /**
     * We don't call ´player.release()´ because we may need the player later
     */
    private fun pausePlayerAndVibrator() {
        vibrator.cancel()
        player.pause()
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

    private fun disableOneShotAlarm(alarm: Alarm) {
        if (!alarm.recurring) {
            alarmRepository.get(alarm.id).observe(this, {
                it?.let {
                    AlarmHandler.disableAlarm(it)
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
    private fun buildAlarmNotification(alarm: Alarm): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

        IntentUtils.safePutAlarmIntoIntent(notificationIntent, alarm)

        val notificationRequestCode = alarm.id.toInt()
        val notificationPendingIntent = PendingIntent.getActivity(
            this,
            notificationRequestCode,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val snoozeIntent = AlarmBroadcastReceiver.getSnoozeActionIntent(this, alarm)
        val snoozePendingIntent: PendingIntent =
            PendingIntent.getBroadcast(
                this,
                notificationRequestCode,
                snoozeIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
        val snoozeActionNotification =
            NotificationCompat.Action(
                null,
                getString(R.string.alarm_snooze).toUpperCase(Locale.getDefault()),
                snoozePendingIntent
            )

        val stopIntent = AlarmBroadcastReceiver.getStopAlarmActionIntent(this, alarm)
        val stopPendingIntent: PendingIntent =
            PendingIntent.getBroadcast(
                this,
                notificationRequestCode,
                stopIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
        val stopActionNotification =
            NotificationCompat.Action(
                null,
                getString(R.string.alarm_turn_off).toUpperCase(Locale.getDefault()),
                stopPendingIntent
            )

        val timeFormatter = SimpleDateFormat.getTimeInstance(DateFormat.SHORT)
        val time = timeFormatter.format(Calendar.getInstance().time)

        return NotificationCompat.Builder(this, ReveryApplication.CHANNEL_ID)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentTitle(String.format("%s %s", getString(R.string.alarm_of), time))
            .setContentText(alarm.label)
            .setSmallIcon(R.drawable.ic_alarm) // TODO change icon
            .setContentIntent(notificationPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setFullScreenIntent(notificationPendingIntent, true)
            .addAction(stopActionNotification)
            .addAction(snoozeActionNotification)
            .setColor(ContextCompat.getColor(this, R.color.colorAccent))
            .setAutoCancel(false)
            .build()
    }

    private fun vibrate() {
        // Vibrate for 1000 milliseconds
        // Sleep for 1000 milliseconds
        val pattern = longArrayOf(1000, 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            vibrator.vibrate(pattern, 0)
        }
    }

    override fun onDestroy() {
        unregisterFromLocalAlarmBroadcastReceiver()
        isRunning = false
        super.onDestroy()
        logError("onDestroy()")
    }
}
