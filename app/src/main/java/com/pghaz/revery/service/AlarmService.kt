package com.pghaz.revery.service

import android.app.Notification
import android.app.NotificationManager
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
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.pghaz.revery.MainActivity
import com.pghaz.revery.R
import com.pghaz.revery.alarm.AlarmHandler
import com.pghaz.revery.broadcastreceiver.AlarmBroadcastReceiver
import com.pghaz.revery.extension.logError
import com.pghaz.revery.model.app.alarm.Alarm
import com.pghaz.revery.model.app.alarm.AlarmMetadata
import com.pghaz.revery.model.app.alarm.MediaType
import com.pghaz.revery.notification.NotificationHandler
import com.pghaz.revery.player.*
import com.pghaz.revery.repository.AlarmRepository
import com.pghaz.revery.settings.SettingsHandler
import com.pghaz.revery.util.IntentUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class AlarmService : LifecycleService(), AbstractPlayer.PlayerListener {

    companion object {
        private const val ACTION_ALARM_SERVICE_SHOULD_STOP =
            "com.pghaz.revery.ACTION_ALARM_SERVICE_SHOULD_STOP"
        private const val ACTION_ALARM_SERVICE_SNOOZE =
            "com.pghaz.revery.ACTION_ALARM_SERVICE_SNOOZE"


        fun getServiceShouldStopIntent(context: Context, alarm: Alarm): Intent {
            val intent =
                Intent(context.applicationContext, AlarmServiceBroadcastReceiver::class.java)
            intent.action = ACTION_ALARM_SERVICE_SHOULD_STOP

            IntentUtils.safePutAlarmIntoIntent(intent, alarm)

            return intent
        }

        fun getServiceSnoozeIntent(context: Context, alarm: Alarm): Intent {
            val intent =
                Intent(context.applicationContext, AlarmServiceBroadcastReceiver::class.java)
            intent.action = ACTION_ALARM_SERVICE_SNOOZE

            IntentUtils.safePutAlarmIntoIntent(intent, alarm)

            return intent
        }

        fun buildSnoozeNotification(context: Context, alarm: Alarm): Notification {
            val notificationRequestCode = alarm.id.toInt()

            val snoozeCancelIntent =
                AlarmBroadcastReceiver.getSnoozeCancelActionIntent(context, alarm)
            val snoozeCancelPendingIntent: PendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    notificationRequestCode,
                    snoozeCancelIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
            val snoozeCancelActionNotification =
                NotificationCompat.Action(
                    null,
                    context.getString(R.string.alarm_disable_snooze)
                        .toUpperCase(Locale.getDefault()),
                    snoozeCancelPendingIntent
                )

            return NotificationCompat.Builder(context, NotificationHandler.CHANNEL_ID_ALARM_SNOOZE)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentTitle(
                    String.format(
                        "%s %02d:%02d",
                        context.getString(R.string.alarm_snooze_of),
                        alarm.hour,
                        alarm.minute
                    )
                )
                .setContentText(alarm.label)
                .setSmallIcon(R.drawable.ic_revery_transparent)
                .setDeleteIntent(snoozeCancelPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .addAction(snoozeCancelActionNotification)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setAutoCancel(false)
                .build()
        }

        var isRunning: Boolean = false // this is ugly: find a way to check if service is alive
    }

    private lateinit var alarm: Alarm

    private lateinit var alarmRepository: AlarmRepository
    private var alarmLiveData: LiveData<Alarm>? = null

    private lateinit var vibrator: Vibrator
    private lateinit var notification: Notification
    private var initialDoNotDisturbMode: Int = -1

    private lateinit var player: AbstractPlayer

    private var receiver = AlarmServiceBroadcastReceiver()

    inner class AlarmServiceBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val alarm = IntentUtils.safeGetAlarmFromIntent(intent)

                if (initialDoNotDisturbMode != -1) {
                    val notificationManager =
                        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    NotificationHandler.setInterruptionFilter(
                        notificationManager,
                        initialDoNotDisturbMode
                    )
                }

                if (ACTION_ALARM_SERVICE_SHOULD_STOP == it.action) {
                    logError(ACTION_ALARM_SERVICE_SHOULD_STOP)
                    stopPlayerAndVibrator(false, alarm.metadata)
                } else if (ACTION_ALARM_SERVICE_SNOOZE == it.action) {
                    logError(ACTION_ALARM_SERVICE_SNOOZE)
                    stopPlayerAndVibrator(true, alarm.metadata)
                }

                player.release()

                killService() // will call onDestroy()
            }
        }
    }

    private val mBinder = AlarmServiceBinder()

    inner class AlarmServiceBinder : Binder() {
        fun getPlayer(): AbstractPlayer? {
            return player
        }

        fun getAlarm(): Alarm {
            return alarm
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
        intentFilter.addAction(ACTION_ALARM_SERVICE_SNOOZE)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter)
    }

    private fun unregisterFromLocalAlarmBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseCrashlytics.getInstance().log("AlarmService.onCreate()")
        isRunning = true

        alarmRepository = AlarmRepository(application)

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        registerToLocalAlarmBroadcastReceiver()
    }

    override fun onStartCommand(alarmIntent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(alarmIntent, flags, startId)

        alarmIntent?.let {
            FirebaseCrashlytics.getInstance().log("AlarmService.onStartCommand() succeed")

            lifecycleScope.launch(Dispatchers.Main) {
                alarm = IntentUtils.safeGetAlarmFromIntent(it)
                val fadeInDuration = SettingsHandler.getFadeInDuration(this@AlarmService)

                notification = buildAlarmNotification(alarm)

                disableOneShotAlarm(this@AlarmService, alarm)

                // Disable DND if app has access and DND is enabled
                disableDoNotDisturbIfNeeded()

                val shouldUseDeviceVolume =
                    SettingsHandler.getShouldUseDeviceVolume(this@AlarmService)

                try {
                    player = if (!this@AlarmService::player.isInitialized) {
                        getInitializedPlayer(
                            alarm.metadata.type,
                            false,
                            shouldUseDeviceVolume,
                            this@AlarmService,
                            alarm.fadeIn,
                            fadeInDuration
                        )
                    } else {
                        stopPlayerAndVibrator(true, alarm.metadata)
                        getInitializedPlayer(
                            alarm.metadata.type,
                            false,
                            shouldUseDeviceVolume,
                            this@AlarmService,
                            alarm.fadeIn,
                            fadeInDuration
                        )
                    }

                    // Additional settings such as shuffle
                    configurePlayer(alarm.metadata)
                    player.prepareAsync(alarm.metadata.uri)
                } catch (exception: Exception) {
                    onPlayerError(PlayerError.Initialization(exception))
                }

                startForeground(NotificationHandler.NOTIFICATION_ID_ALARM, notification)
            }
        } ?: kotlin.run {
            FirebaseCrashlytics.getInstance().log("AlarmService.onStartCommand() failed")
            killService()
        }

        return START_STICKY
    }

    private suspend fun disableDoNotDisturbIfNeeded() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (NotificationHandler.isNotificationPolicyAccessGranted(notificationManager)) {
            if (NotificationHandler.isDoNotDisturbEnabled(notificationManager)) {

                FirebaseCrashlytics.getInstance().log("AlarmService.disableDoNotDisturbIfNeeded()")

                initialDoNotDisturbMode = notificationManager.currentInterruptionFilter

                NotificationHandler.setInterruptionFilter(
                    notificationManager,
                    NotificationManager.INTERRUPTION_FILTER_ALL
                )
                // Delay for a sec because the change is not instant on some devices
                delay(1000)
            }
        }
    }

    private fun killService() {
        FirebaseCrashlytics.getInstance().log("AlarmService.killService()")
        stopForeground(true)
        stopSelf()
    }

    private fun configurePlayer(metadata: AlarmMetadata) {
        when (metadata.type) {
            MediaType.SPOTIFY_ALBUM,
            MediaType.SPOTIFY_ARTIST,
            MediaType.SPOTIFY_PLAYLIST,
            MediaType.SPOTIFY_TRACK -> {
                (player as SpotifyPlayer).shuffle = metadata.shuffle
                (player as SpotifyPlayer).repeat = metadata.repeat
            }

            MediaType.DEFAULT -> {
                // do nothing
            }
        }
    }

    private fun getInitializedPlayer(
        type: MediaType,
        isEmergencyAlarm: Boolean,
        shouldUseDeviceVolume: Boolean,
        playerListener: AbstractPlayer.PlayerListener?,
        fadeIn: Boolean,
        fadeInDuration: Long,
    ): AbstractPlayer {
        return when (type) {
            MediaType.SPOTIFY_ALBUM,
            MediaType.SPOTIFY_ARTIST,
            MediaType.SPOTIFY_PLAYLIST,
            MediaType.SPOTIFY_TRACK -> {
                SpotifyPlayer(this, isEmergencyAlarm, shouldUseDeviceVolume)
            }
            else -> {
                DefaultPlayer(this, isEmergencyAlarm, shouldUseDeviceVolume)
            }
        }.apply {
            this.init(playerListener)
            this.fadeIn = fadeIn
            this.fadeInDuration = fadeInDuration
        }
    }

    /**
     * We don't call ´player.release()´ because we may need the player later
     */
    private fun stopPlayerAndVibrator(forceShouldPausePlayback: Boolean, metadata: AlarmMetadata) {
        FirebaseCrashlytics.getInstance()
            .log("AlarmService.stopPlayerAndVibrator($forceShouldPausePlayback, $metadata)")

        vibrator.cancel()

        if (forceShouldPausePlayback) {
            player.stop()
            return
        }

        if (metadata.type == MediaType.DEFAULT) {
            player.stop()
        } else if (!metadata.shouldKeepPlaying) {
            player.stop()
        }
    }

    override fun onPlayerInitialized(player: AbstractPlayer) {
        FirebaseCrashlytics.getInstance().log("AlarmService.onPlayerInitialized()")

        if (alarm.vibrate) {
            vibrate()
        }

        player.start()
    }

    override fun onPlayerError(error: PlayerError) {
        logError("onPlayerError(): Play emergency alarm")
        logError("onPlayerError(): $error")

        FirebaseCrashlytics.getInstance().log("AlarmService.onPlayerError()")

        FirebaseCrashlytics.getInstance().recordException(error)

        val shouldUseDeviceVolume = SettingsHandler.getShouldUseDeviceVolume(this)

        notifyErrorOccurred(this, error)

        playEmergencyAlarm(error, shouldUseDeviceVolume)
    }

    private fun playEmergencyAlarm(error: PlayerError, shouldUseDeviceVolume: Boolean) {
        FirebaseCrashlytics.getInstance().log("AlarmService.playEmergencyAlarm()")

        if (this::player.isInitialized) {
            FirebaseCrashlytics.getInstance()
                .log("AlarmService.playEmergencyAlarm() player was initialized")
            vibrator.cancel()

            if (error !is SpotifyPlayerError) {
                FirebaseCrashlytics.getInstance()
                    .log("AlarmService.playEmergencyAlarm() Spotify error")
                player.release()
            }
        }

        player = getInitializedPlayer(
            MediaType.DEFAULT,
            isEmergencyAlarm = true,
            shouldUseDeviceVolume = shouldUseDeviceVolume,
            playerListener = null,
            fadeIn = false,
            fadeInDuration = 0
        )
        player.prepare(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString())

        onPlayerInitialized(player)
    }

    private fun notifyErrorOccurred(context: Context, error: PlayerError) {
        val notificationBuilder =
            NotificationCompat.Builder(context, NotificationHandler.CHANNEL_ID_ALARM_ERROR)
                .setCategory(NotificationCompat.CATEGORY_ERROR)
                .setContentTitle(context.getString(R.string.emergency_alarm))
                .setSmallIcon(R.drawable.ic_revery_transparent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setAutoCancel(true)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(getString(R.string.rescheduling_alarms_after_reboot))
                )

        val text = if (error is SpotifyPlayerError) {
            when (error) {
                is SpotifyPlayerError.SpotifyAuthenticationFailed -> {
                    context.getString(R.string.notification_spotify_error_authentication_failed)
                }
                is SpotifyPlayerError.SpotifyOfflineMode -> {
                    context.getString(R.string.notification_spotify_error_offline)
                }
                is SpotifyPlayerError.SpotifyPlayerNotInstalled -> {
                    context.getString(R.string.notification_spotify_error_not_installed)
                }
                is SpotifyPlayerError.SpotifyPlayerUserNotAuthorized -> {
                    context.getString(R.string.notification_spotify_error_player_user_not_authorized)
                }
                is SpotifyPlayerError.SpotifyNotLoggedIn -> {
                    context.getString(R.string.notification_spotify_error_not_logged_in)
                }
                is SpotifyPlayerError.SpotifyUnsupportedFeatureVersion -> {
                    context.getString(R.string.notification_spotify_error_unsupported_feature_version)
                }
                is SpotifyPlayerError.SpotifyRemoteService -> {
                    context.getString(R.string.notification_spotify_error_remote_service)
                }
                is SpotifyPlayerError.SpotifyDisconnected -> {
                    context.getString(R.string.notification_spotify_error_disconnected)
                }
                else -> {
                    context.getString(R.string.notification_spotify_error_unknown)
                }
            }
        } else {
            context.getString(R.string.notification_player_error_general)
        }

        val notification = notificationBuilder
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .build()

        NotificationHandler.notify(
            context,
            NotificationHandler.NOTIFICATION_ID_ERROR_OCCURRED,
            notification
        )
    }

    private fun disableOneShotAlarm(context: Context?, alarm: Alarm) {
        if (!alarm.recurring && !alarm.isSnooze && !alarm.isPreview) {
            FirebaseCrashlytics.getInstance().log("AlarmService.disableOneShotAlarm()")
            alarmLiveData = alarmRepository.get(alarm)
            alarmLiveData?.observe(this, {
                it?.let {
                    AlarmHandler.cancelAlarm(context, it)
                    alarmRepository.update(it)
                }

                // Important: we remove the observer because otherwise, as we change the value inside the observer
                // it would call the observer in a infinite loop
                alarmLiveData?.removeObservers(this)
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

        return NotificationCompat.Builder(this, NotificationHandler.CHANNEL_ID_ALARM)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentTitle(String.format("%s %s", getString(R.string.alarm_of), time))
            .setContentText(alarm.label)
            .setSmallIcon(R.drawable.ic_revery_transparent)
            .setContentIntent(notificationPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setFullScreenIntent(notificationPendingIntent, true)
            .addAction(stopActionNotification)
            .addAction(snoozeActionNotification)
            .setColor(ContextCompat.getColor(this, R.color.colorAccent))
            .setAutoCancel(false)
            .setStyle(NotificationCompat.BigTextStyle().bigText(alarm.label))
            .build()
    }

    @Suppress("DEPRECATION")
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
        FirebaseCrashlytics.getInstance().log("AlarmService.onDestroy()")
    }
}
