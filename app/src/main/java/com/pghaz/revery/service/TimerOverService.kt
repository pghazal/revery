package com.pghaz.revery.service

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.pghaz.revery.MainActivity
import com.pghaz.revery.R
import com.pghaz.revery.broadcastreceiver.TimerBroadcastReceiver
import com.pghaz.revery.extension.logError
import com.pghaz.revery.model.app.Timer
import com.pghaz.revery.notification.NotificationHandler
import com.pghaz.revery.player.AbstractPlayer
import com.pghaz.revery.player.DefaultPlayer
import com.pghaz.revery.player.PlayerError
import com.pghaz.revery.repository.TimerRepository
import com.pghaz.revery.settings.SettingsHandler
import com.pghaz.revery.timer.TimerHandler
import com.pghaz.revery.util.Arguments
import com.pghaz.revery.util.IntentUtils
import java.util.*

class TimerOverService : LifecycleService(), AbstractPlayer.PlayerListener {

    companion object {
        private const val ACTION_TIMER_SHOULD_STOP =
            "com.pghaz.revery.ACTION_TIMER_SHOULD_STOP"
        private const val ACTION_TIMER_INCREMENT =
            "com.pghaz.revery.ACTION_TIMER_INCREMENT"

        fun getTimerShouldStopIntent(context: Context, timer: Timer): Intent {
            val intent = Intent(
                context.applicationContext,
                TimerOverServiceBroadcastReceiver::class.java
            )
            intent.action = ACTION_TIMER_SHOULD_STOP

            IntentUtils.safePutTimerIntoIntent(intent, timer)

            return intent
        }

        fun getTimerIncrementIntent(context: Context, timer: Timer): Intent {
            val intent = Intent(
                context.applicationContext,
                TimerOverServiceBroadcastReceiver::class.java
            )
            intent.action = ACTION_TIMER_INCREMENT

            IntentUtils.safePutTimerIntoIntent(intent, timer)

            return intent
        }
    }

    inner class TimerOverServiceBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val timer = IntentUtils.safeGetTimerFromIntent(intent)

                // This is called when 1) timer is ringing and
                // 2) user clicked stop from view holder or from the notification.
                // 3) user clicked "+1min" from view holder while timer is ringing.
                if (ACTION_TIMER_SHOULD_STOP == it.action) {
                    logError(ACTION_TIMER_SHOULD_STOP)
                    if (currentTimer?.id == timer.id) {
                        val isIncrementAction =
                            it.getBooleanExtra(Arguments.ARGS_TIMER_INCREMENT, false)

                        // Stop the timer and save it in DB
                        if (!isIncrementAction) {
                            TimerHandler.resetTimer(timer)
                            timerRepository.update(timer)
                        }

                        player.stop()
                    } else {
                        synchronized(timersOverQueue) {
                            timersOverQueue.forEach { item ->
                                if (item.id == timer.id) {
                                    timersOverQueue.remove(item)
                                }
                            }
                        }
                    }
                }
                // This is called when user clicked "+1 min" from the notification ONLY
                else if (ACTION_TIMER_INCREMENT == it.action) {
                    logError(ACTION_TIMER_INCREMENT)

                    if (currentTimer?.id == timer.id) {
                        TimerHandler.pauseTimer(timer)
                        TimerHandler.removeAlarm(context, timer)
                        TimerHandler.incrementTimer(timer)
                        TimerHandler.startTimer(timer)
                        TimerHandler.setAlarm(context, timer)
                        timerRepository.update(timer)

                        player.stop()
                    }
                }
            }
        }
    }

    private var receiver = TimerOverServiceBroadcastReceiver()

    private lateinit var vibrator: Vibrator
    private lateinit var player: DefaultPlayer
    private lateinit var notification: Notification

    private lateinit var timerRepository: TimerRepository

    private val timersOverQueue: Queue<Timer> = LinkedList()
    private var currentTimer: Timer? = null

    private fun registerToLocalTimerOverBroadcastReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_TIMER_SHOULD_STOP)
        intentFilter.addAction(ACTION_TIMER_INCREMENT)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter)
    }

    private fun unregisterFromLocalTimerOverBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    override fun onCreate() {
        super.onCreate()

        timerRepository = TimerRepository(application)

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        registerToLocalTimerOverBroadcastReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        intent?.let { nonNullIntent ->
            val timerOver = IntentUtils.safeGetTimerFromIntent(nonNullIntent)

            queue(timerOver)

            // First time Service is started
            if (currentTimer == null) {
                currentTimer = pop()

                currentTimer?.let {
                    notification = buildTimerNotification(it)

                    if (!this::player.isInitialized) {
                        val shouldUseDeviceVolume =
                            SettingsHandler.getShouldUseDeviceVolume(this@TimerOverService)
                        player = DefaultPlayer(this, false, shouldUseDeviceVolume)
                        player.init(this@TimerOverService)
                        player.prepareAsync(it.metadata.uri)
                    }

                    startForeground(
                        NotificationHandler.NOTIFICATION_ID_TIMER_OVER,
                        notification
                    )
                }
            }
        }

        return START_STICKY
    }

    private fun queue(timer: Timer) {
        synchronized(timersOverQueue) {
            if (!timersOverQueue.contains(timer)) {
                timersOverQueue.add(timer)
            }
        }
    }

    private fun pop(): Timer? {
        synchronized(timersOverQueue) {
            return timersOverQueue.poll()
        }
    }

    override fun onDestroy() {
        logError("onDestroy()")
        unregisterFromLocalTimerOverBroadcastReceiver()
        super.onDestroy()
    }

    override fun onPlayerInitialized(player: AbstractPlayer) {
        if (currentTimer?.vibrate == true) {
            vibrate()
        }

        player.play()
    }

    override fun onPlayerStopped(player: AbstractPlayer) {
        vibrator.cancel()

        player.release()
    }

    override fun onPlayerReleased(player: AbstractPlayer) {
        currentTimer = pop()

        currentTimer?.let {
            player.init(this)
            player.prepareAsync(it.metadata.uri)

            notification = buildTimerNotification(it)
            NotificationHandler.notify(
                this, NotificationHandler.NOTIFICATION_ID_TIMER_OVER,
                notification
            )

        } ?: kotlin.run {
            killService() // will call onDestroy()
        }
    }

    override fun onPlayerError(error: PlayerError) {
        // do nothing
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

    /**
     * The MOST IMPORTANT thing to remember here in order to start the Activity in the Service is:
     *  - set a requestCode different than 0 in the PendingIntent.getActivity()
     *  - add .setFullScreenIntent(pendingIntent, true) when creating the notification
     */
    private fun buildTimerNotification(timer: Timer): Notification {
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

        val stopIntent = TimerBroadcastReceiver.getStopTimerActionIntent(this, timer)
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

        val incrementIntent = TimerBroadcastReceiver.getTimerIncrementActionIntent(this, timer)
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

        return NotificationCompat.Builder(this, NotificationHandler.CHANNEL_ID_TIMER_OVER)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentTitle(String.format("%s", getString(R.string.timer_is_over)))
            .setContentText(timer.label)
            .setSmallIcon(R.drawable.ic_revery_transparent)
            .setContentIntent(notificationPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setFullScreenIntent(notificationPendingIntent, true)
            .addAction(stopActionNotification)
            .addAction(incrementActionNotification)
            .setColor(ContextCompat.getColor(this, R.color.colorAccent))
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(timer.label))
            .build()
    }

    private fun killService() {
        FirebaseCrashlytics.getInstance().log("TimerOverService.killService()")
        stopForeground(true)
        stopSelf()
    }
}