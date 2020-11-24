package com.pghaz.revery.service

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.pghaz.revery.R
import com.pghaz.revery.extension.logError
import com.pghaz.revery.model.app.StandByEnabler
import com.pghaz.revery.notification.NotificationHandler
import com.pghaz.revery.player.AbstractPlayer
import com.pghaz.revery.player.PlayerError
import com.pghaz.revery.player.SpotifyPlayer
import com.pghaz.revery.settings.SettingsHandler
import com.pghaz.revery.standby.StandByFragment
import com.pghaz.revery.standby.StandByHandler
import com.pghaz.revery.util.IntentUtils

class StandByService : LifecycleService(), AbstractPlayer.PlayerListener {

    private var player: SpotifyPlayer? = null

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notification: Notification

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (intent == null) {
            stopForeground(true)
            stopSelf()
        } else {
            val standByEnabler = IntentUtils.safeGetStandByEnablerFromIntent(intent)

            // Re-enable for tomorrow
            StandByHandler.setAlarm(this, DateFormat.is24HourFormat(this), standByEnabler)
            broadcastStandByStarted(this, standByEnabler)

            val shouldUseDeviceVolume =
                SettingsHandler.getShouldUseDeviceVolume(this)
            player = SpotifyPlayer(this, false, shouldUseDeviceVolume)

            notificationBuilder = buildNotification()
            notification = notificationBuilder.build()

            startForeground(
                NotificationHandler.NOTIFICATION_ID_TIMER_RUNNING,
                notification
            )

            player?.apply {
                this.init(this@StandByService)
                this.fadeOut = standByEnabler.fadeOut
                this.fadeOutDuration = standByEnabler.fadeOutDuration
                // Setting empty string because we just want to connect and stop player if needed
                this.prepareAsync("")
            }
        }

        return START_STICKY
    }

    private fun broadcastStandByStarted(context: Context, standByEnabler: StandByEnabler) {
        val intent = StandByFragment.buildStandByStartedIntent(context, standByEnabler)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun buildNotification(): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, NotificationHandler.CHANNEL_ID_TIMER)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentTitle(String.format("%s", getString(R.string.standby_text)))
            .setContentText(String.format("%s", getString(R.string.standby_start_text)))
            .setSmallIcon(R.drawable.ic_revery_transparent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setColor(ContextCompat.getColor(this, R.color.colorAccent))
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(String.format("%s", getString(R.string.standby_start_text)))
            )
    }

    override fun onPlayerInitialized(player: AbstractPlayer) {
        val spotifyPlayer = player as SpotifyPlayer
        spotifyPlayer.getPlayerStateCallResult()?.setResultCallback {
            if (it.track != null && !it.isPaused) {
                if (player.fadeOut) {
                    player.fadeOut()
                } else {
                    player.stop()
                }
            } else {
                player.release()
            }
        }
    }

    override fun onPlayerStopped(player: AbstractPlayer) {
        player.release()
    }

    override fun onPlayerReleased(player: AbstractPlayer) {
        killService()
    }

    override fun onPlayerError(error: PlayerError) {
    }

    private fun killService() {
        FirebaseCrashlytics.getInstance().log("StandByService.killService()")
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        logError("onDestroy()")
        super.onDestroy()
    }
}