package com.pghaz.revery.service

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.pghaz.revery.MainActivity
import com.pghaz.revery.R
import com.pghaz.revery.broadcastreceiver.TimerBroadcastReceiver
import com.pghaz.revery.extension.logError
import com.pghaz.revery.model.app.MediaType
import com.pghaz.revery.model.app.Timer
import com.pghaz.revery.notification.NotificationHandler
import com.pghaz.revery.player.AbstractPlayer
import com.pghaz.revery.player.PlayerError
import com.pghaz.revery.player.SpotifyPlayer
import com.pghaz.revery.repository.TimerRepository
import com.pghaz.revery.settings.SettingsHandler
import com.pghaz.revery.timer.TimerHandler
import com.pghaz.revery.util.IntentUtils
import com.spotify.protocol.types.Repeat
import java.util.*
import kotlin.collections.ArrayList

class TimerRunningService : LifecycleService(), AbstractPlayer.PlayerListener {

    companion object {
        private const val ACTION_TIMER_RUNNING_STOP = "com.pghaz.revery.ACTION_TIMER_RUNNING_STOP"
        private const val ACTION_TIMER_RUNNING_INCREMENT =
            "com.pghaz.revery.ACTION_TIMER_RUNNING_INCREMENT"

        fun buildRunningTimerShouldStopIntent(context: Context, timer: Timer): Intent {
            val intent = Intent(
                context.applicationContext,
                TimerRunningServiceBroadcastReceiver::class.java
            )
            intent.action = ACTION_TIMER_RUNNING_STOP

            IntentUtils.safePutTimerIntoIntent(intent, timer)

            return intent
        }

        fun buildRunningTimerIncrementIntent(context: Context, timer: Timer): Intent {
            val intent = Intent(
                context.applicationContext,
                TimerRunningServiceBroadcastReceiver::class.java
            )
            intent.action = ACTION_TIMER_RUNNING_INCREMENT

            IntentUtils.safePutTimerIntoIntent(intent, timer)

            return intent
        }
    }

    inner class TimerRunningServiceBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (context == null || intent == null) {
                return
            }

            val timer = IntentUtils.safeGetTimerFromIntent(intent)

            // This is called when user clicked "+1 min" from the notification ONLY
            if (ACTION_TIMER_RUNNING_INCREMENT == intent.action) {
                logError(ACTION_TIMER_RUNNING_INCREMENT)

                TimerHandler.pauseTimer(timer)
                TimerHandler.removeAlarm(context, timer)
                TimerHandler.incrementTimer(timer)
                TimerHandler.startTimer(timer)
                TimerHandler.setAlarm(context, timer)
                timerRepository.update(timer)

            } else if (ACTION_TIMER_RUNNING_STOP == intent.action) {
                logError(ACTION_TIMER_RUNNING_STOP)

                player.release()

                // Remove timer which has been stopped
                remove(timer)

                if (timersRunningList.isEmpty()) {
                    killService()
                } else {
                    // Handle next ordered timer
                    currentTimer = timersRunningList[0]
                    // If there is one, build notification, notify and play music if needed
                    // If it's a spotify content
                    currentTimer?.let {
                        handleTimer(it)
                    }
                }
            }
        }
    }

    private var receiver = TimerRunningServiceBroadcastReceiver()

    private var timerLiveData: LiveData<Timer>? = null
    private lateinit var timerRepository: TimerRepository

    private val timersRunningList = ArrayList<Timer>()
    private var currentTimer: Timer? = null

    private lateinit var player: SpotifyPlayer

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notification: Notification
    private lateinit var javaTimer: java.util.Timer

    private fun createNotificationUpdateTimerTask(): TimerTask {
        return object : TimerTask() {
            override fun run() {
                currentTimer?.let { updateNotificationProgress(it) }
            }
        }
    }

    private fun stopUpdateNotification() {
        if (this::javaTimer.isInitialized) {
            javaTimer.cancel()
            javaTimer.purge()
        }
    }

    private fun registerToLocalTimerOverBroadcastReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_TIMER_RUNNING_STOP)
        intentFilter.addAction(ACTION_TIMER_RUNNING_INCREMENT)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter)
    }

    private fun unregisterFromLocalTimerOverBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    override fun onCreate() {
        super.onCreate()

        timerRepository = TimerRepository(application)

        registerToLocalTimerOverBroadcastReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        intent?.let { nonNullIntent ->
            val timerRunning = IntentUtils.safeGetTimerFromIntent(nonNullIntent)

            timerLiveData = timerRepository.get(timerRunning)
            timerLiveData?.observe(this, { timerFromDb ->
                timerLiveData?.removeObservers(this)

                add(timerFromDb)
                sortTimers()

                currentTimer = timersRunningList[0]

                currentTimer?.let {
                    if (!this::player.isInitialized) {
                        val shouldUseDeviceVolume =
                            SettingsHandler.getShouldUseDeviceVolume(this@TimerRunningService)
                        player = SpotifyPlayer(this, false, shouldUseDeviceVolume)
                    }

                    notificationBuilder = buildTimerNotification(it)
                    notification = notificationBuilder.build()

                    startForeground(
                        NotificationHandler.NOTIFICATION_ID_TIMER_RUNNING,
                        notification
                    )

                    // Here we put timerFromDb because we want to configure the player with the
                    // incoming timer which may have Spotify content
                    handleTimer(timerFromDb)
                }
            })
        }

        return START_STICKY
    }

    private fun handleTimer(timer: Timer) {
        // If it's a spotify content
        if (timer.metadata.type != MediaType.DEFAULT) {
            player.apply {
                this.init(this@TimerRunningService)
                configurePlayer(timer, this)
                this.prepareAsync(timer.metadata.uri)
            }
        }

        stopUpdateNotification()

        javaTimer = java.util.Timer()
        javaTimer.schedule(createNotificationUpdateTimerTask(), 0L, 1000L)
    }

    private fun updateNotificationProgress(timer: Timer) {
        val remainingTime = TimerHandler.getRemainingTime(timer)

        val milliseconds = if (remainingTime > 0) {
            remainingTime
        } else {
            timer.duration
        }

        val seconds = (milliseconds / 1000).toInt() % 60
        val minutes = (milliseconds / (1000 * 60) % 60).toInt()
        val hours = (milliseconds / (1000 * 60 * 60) % 24).toInt()

        val hourText = if (hours > 0) {
            String.format("%02d%s", hours, getString(R.string.text_hour_short))
        } else {
            ""
        }

        val minuteText = String.format("%02d%s", minutes, getString(R.string.text_minute_short))

        var secondText = if (seconds > 0) {
            String.format("%02d%s", seconds, getString(R.string.text_second_short))
        } else {
            ""
        }

        if (hours == 0 && seconds == 0) {
            secondText = String.format("00%s", getString(R.string.text_second_short))
        }

        val progressText = String.format("%s %s %s", hourText, minuteText, secondText)

        notificationBuilder.setContentTitle(
            String.format(
                "%s %s",
                getString(R.string.remaining_time),
                progressText
            )
        ).setContentText(timer.label)
            .setStyle(NotificationCompat.BigTextStyle().bigText(timer.label))

        notification = notificationBuilder.build()
        NotificationHandler.notify(
            this,
            NotificationHandler.NOTIFICATION_ID_TIMER_RUNNING,
            notification
        )
    }

    private fun configurePlayer(timer: Timer, player: SpotifyPlayer) {
        player.fadeIn = timer.fadeOut
        player.fadeInDuration = 10000 // TODO
        player.shuffle = timer.metadata.shuffle
        player.repeat = Repeat.ALL
    }

    private fun add(timer: Timer) {
        synchronized(timersRunningList) {
            var found = false
            timersRunningList.forEach {
                if (it.id == timer.id) {
                    found = true
                    return@forEach
                }
            }

            if (!found) {
                timersRunningList.add(timer)
            }
        }
    }

    private fun sortTimers() {
        synchronized(timersRunningList) {
            timersRunningList.sortBy {
                it.remainingTime
            }
        }
    }

    private fun remove(timer: Timer) {
        synchronized(timersRunningList) {
            var indexToRemove = -1
            timersRunningList.forEachIndexed { index, item ->
                if (item.id == timer.id) {
                    indexToRemove = index
                    return@forEachIndexed
                }
            }
            if (indexToRemove != -1) {
                timersRunningList.removeAt(indexToRemove)
            }
        }
    }

    override fun onPlayerInitialized(player: AbstractPlayer) {
        val spotifyPlayer = player as SpotifyPlayer

        spotifyPlayer.getPlayerStateCallResult()?.setResultCallback {
            if (it.track != null && !it.isPaused) {
                // Let's player continue
                // kill the service
                player.release()
            } else {
                player.start()
            }
        }
    }

    override fun onPlayerStopped(player: AbstractPlayer) {
        player.release()
    }

    override fun onPlayerReleased(player: AbstractPlayer) {
        // do nothing
    }

    override fun onPlayerError(error: PlayerError) {
        FirebaseCrashlytics.getInstance().recordException(error)
    }

    /**
     * The MOST IMPORTANT thing to remember here in order to start the Activity in the Service is:
     *  - set a requestCode different than 0 in the PendingIntent.getActivity()
     *  - add .setFullScreenIntent(pendingIntent, true) when creating the notification
     */
    private fun buildTimerNotification(timer: Timer): NotificationCompat.Builder {
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

        IntentUtils.safePutTimerIntoIntent(notificationIntent, timer)

        val notificationRequestCode = timer.id.toInt()
        val notificationPendingIntent = PendingIntent.getActivity(
            this,
            notificationRequestCode,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

/*        val stopIntent = TimerBroadcastReceiver.getStopTimerActionIntent(this, timer)
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
            )*/

        val incrementIntent =
            TimerBroadcastReceiver.buildRunningTimerIncrementActionIntent(this, timer)
        val incrementPendingIntent: PendingIntent =
            PendingIntent.getBroadcast(
                this,
                notificationRequestCode,
                incrementIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
        val incrementActionNotification =
            NotificationCompat.Action(
                null,
                getString(R.string.timer_increment).toUpperCase(Locale.getDefault()),
                incrementPendingIntent
            )

        return NotificationCompat.Builder(this, NotificationHandler.CHANNEL_ID_TIMER)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentTitle(String.format("%s", getString(R.string.remaining_time)))
            .setContentText(timer.label)
            .setSmallIcon(R.drawable.ic_revery_transparent)
            .setContentIntent(notificationPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            //.addAction(stopActionNotification)
            .addAction(incrementActionNotification)
            .setColor(ContextCompat.getColor(this, R.color.colorAccent))
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(timer.label))
    }

    override fun onDestroy() {
        logError("onDestroy()")
        stopUpdateNotification()
        unregisterFromLocalTimerOverBroadcastReceiver()
        super.onDestroy()
    }

    private fun killService() {
        FirebaseCrashlytics.getInstance().log("TimerRunningService.killService()")
        stopForeground(true)
        stopSelf()
    }
}