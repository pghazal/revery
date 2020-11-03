package com.pghaz.revery.alarm

import android.app.Activity
import android.app.KeyguardManager
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.view.WindowManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.pghaz.revery.BaseActivity
import com.pghaz.revery.R
import com.pghaz.revery.broadcastreceiver.AlarmBroadcastReceiver
import com.pghaz.revery.extension.logError
import com.pghaz.revery.image.ImageLoader
import com.pghaz.revery.image.ImageUtils
import com.pghaz.revery.model.app.alarm.Alarm
import com.pghaz.revery.player.AbstractPlayer
import com.pghaz.revery.player.SpotifyPlayer
import com.pghaz.revery.service.AlarmService
import com.pghaz.revery.settings.SettingsHandler
import com.pghaz.revery.settings.SnoozeDuration
import com.pghaz.revery.util.Arguments
import com.pghaz.revery.util.IntentUtils
import com.pghaz.revery.util.ViewUtils
import com.pghaz.revery.view.OnSwipeTouchListener
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track
import kotlinx.android.synthetic.main.activity_ring.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*

@ExperimentalCoroutinesApi
class RingActivity : BaseActivity() {

    companion object {
        const val REQUEST_CODE_ALARM_RINGING = 42
        const val ACTION_FINISH_RING_ACTIVITY = "com.pghaz.revery.ACTION_FINISH_RING_ACTIVITY"

        fun getFinishRingActivityBroadcastReceiver(context: Context): Intent {
            val intent =
                Intent(context.applicationContext, FinishRingActivityBroadcastReceiver::class.java)
            intent.action = ACTION_FINISH_RING_ACTIVITY

            return intent
        }
    }

    private var player: AbstractPlayer? = null
    private var hasStartedPlayingAtLeast = false
    private var snoozeDurationIndex = 0

    private var mAlarmServiceBound: Boolean = false
    private lateinit var alarm: Alarm

    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            logError("onServiceConnected")
            mAlarmServiceBound = true
            player = (service as AlarmService.AlarmServiceBinder).getPlayer()

            configurePlayerControllers(player)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mAlarmServiceBound = false
            player = null
        }
    }

    inner class FinishRingActivityBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                if (ACTION_FINISH_RING_ACTIVITY == it.action) {
                    logError(ACTION_FINISH_RING_ACTIVITY)
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        }
    }

    private var receiver = FinishRingActivityBroadcastReceiver()

    override fun getLayoutResId(): Int {
        return R.layout.activity_ring
    }

    override fun shouldAnimateOnCreate(): Boolean {
        return false
    }

    override fun shouldAnimateOnFinish(): Boolean {
        return true
    }

    private fun registerToLocalAlarmBroadcastReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_FINISH_RING_ACTIVITY)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter)
    }

    private fun bindToAlarmService() {
        val service = Intent(applicationContext, AlarmService::class.java)
        bindService(service, mServiceConnection, Activity.BIND_AUTO_CREATE)
    }

    private fun unregisterFromLocalAlarmBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    private fun unbindFromAlarmService() {
        if (mAlarmServiceBound) {
            unbindService(mServiceConnection)
            mAlarmServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        allowDisplayOnLockScreen()

        registerToLocalAlarmBroadcastReceiver()
        bindToAlarmService()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        IntentUtils.safePutAlarmIntoBundle(outState, alarm)
        super.onSaveInstanceState(outState)
    }

    override fun parseArguments(args: Bundle?) {
        alarm = IntentUtils.safeGetAlarmFromBundle(args)
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        updateBackgroundImage(alarm.metadata.imageUrl)

        turnOffButton.setOnClickListener {
            broadcastStopAlarm()
        }

        val snoozeDurationArray = resources.getStringArray(R.array.snooze_duration_array)
        snoozeDurationIndex = SettingsHandler.getSnoozeDurationPosition(this)

        minusSnoozeButton.setOnClickListener {
            snoozeDurationIndex -= 1
            if (snoozeDurationIndex <= 0) {
                snoozeDurationIndex = 0
            }

            snoozeButton.text =
                String.format(
                    Locale.getDefault(),
                    "%s\n%s",
                    getString(R.string.alarm_snooze),
                    snoozeDurationArray[snoozeDurationIndex]
                )
        }

        plusSnoozeButton.setOnClickListener {
            snoozeDurationIndex += 1
            if (snoozeDurationIndex >= snoozeDurationArray.size) {
                snoozeDurationIndex = snoozeDurationArray.size - 1
            }

            snoozeButton.text =
                String.format(
                    Locale.getDefault(),
                    "%s\n%s",
                    getString(R.string.alarm_snooze),
                    snoozeDurationArray[snoozeDurationIndex]
                )
        }

        snoozeButton.text =
            String.format(
                Locale.getDefault(),
                "%s\n%s",
                getString(R.string.alarm_snooze),
                snoozeDurationArray[snoozeDurationIndex]
            )

        snoozeButton.setOnClickListener {
            broadcastSnooze(SnoozeDuration.values()[snoozeDurationIndex])
        }

        if (SettingsHandler.getCanChangeSnoozeDuration(this)) {
            minusSnoozeButton.visibility = View.VISIBLE
            plusSnoozeButton.visibility = View.VISIBLE
        } else {
            minusSnoozeButton.visibility = View.INVISIBLE
            plusSnoozeButton.visibility = View.INVISIBLE
        }

        // Player Controllers
        playPauseButton.setOnClickListener {
            if (player is SpotifyPlayer) {
                (player as SpotifyPlayer?)?.getPlayerStateCallResult()
                    ?.setResultCallback { playerState ->
                        if (playerState.isPaused) {
                            player?.play()
                        } else {
                            player?.pause()
                        }
                    }
            }
        }

        skipNextButton.setOnClickListener {
            if (player is SpotifyPlayer) {
                (player as SpotifyPlayer?)?.skipNext()
            }
        }

        skipPreviousButton.setOnClickListener {
            if (player is SpotifyPlayer) {
                (player as SpotifyPlayer?)?.skipPrevious()
            }
        }

        gesturesInterceptorView.setOnTouchListener(object :
            OnSwipeTouchListener(this@RingActivity) {
            override fun onSwipeLeft() {
                super.onSwipeLeft()
                if (player is SpotifyPlayer) {
                    (player as SpotifyPlayer?)?.skipPrevious()
                }
            }

            override fun onSwipeRight() {
                super.onSwipeRight()
                if (player is SpotifyPlayer) {
                    (player as SpotifyPlayer?)?.skipNext()
                }
            }
        })
    }

    private fun configurePlayerControllers(player: AbstractPlayer?) {
        if (player != null && player is SpotifyPlayer) {
            controllersContainer.visibility = View.VISIBLE

            player.playerConnectedLiveData.observe(this, { isPlayerConnected ->
                if (isPlayerConnected) {
                    player.getPlayerStateSubscription()?.setEventCallback { playerState ->
                        val track: Track? = playerState.track

                        if (track != null) {
                            logError(track.name.toString() + " by " + track.artist.name)

                            if (playerState.isPaused) {
                                playPauseButton.setImageResource(R.drawable.ic_play)
                                // If Spotify starting playing and we're pausing from app or Spotify
                                // Let's just stop the
                                if (hasStartedPlayingAtLeast) {
                                    broadcastStopAlarm()
                                }
                            } else {
                                hasStartedPlayingAtLeast = true
                                playPauseButton.setImageResource(R.drawable.ic_pause)

                                titleTextView.text = track.name
                                artistNameTextView.text = track.artist.name
                                updateImageWithSpotify(player, playerState)
                            }
                        }
                    }
                }
            })
        } else {
            controllersContainer.visibility = View.GONE
        }
    }

    private fun updateImageWithSpotify(player: SpotifyPlayer, playerState: PlayerState) {
        player.getImageUri(playerState.track.imageUri)?.setResultCallback {
            val imageUrl = ImageUtils.getSpotifyImageFilePath(
                this@RingActivity,
                playerState.track.imageUri.raw,
                it
            )
            updateBackgroundImage(imageUrl)
        }
    }

    private fun updateBackgroundImage(url: String?) {
        ImageLoader.get()
            .load(url)
            .blur()
            .ratioAndWidth(1f, ViewUtils.getRealScreenWidthSize(backgroundImageView.context), true)
            .into(backgroundImageView)
    }

    private fun broadcastStopAlarm() {
        val stopIntent = AlarmBroadcastReceiver.getStopAlarmActionIntent(applicationContext, alarm)
        sendBroadcast(stopIntent)
    }

    private fun broadcastSnooze(snoozeDuration: SnoozeDuration) {
        val snoozeIntent = AlarmBroadcastReceiver.getSnoozeActionIntent(applicationContext, alarm)
        snoozeIntent.putExtra(Arguments.ARGS_SNOOZE_DURATION, snoozeDuration.minutes)
        sendBroadcast(snoozeIntent)
    }

    // If this activity exists, it means an alarm is ringing.
    // By setting result RESULT_CANCELED, we say to the MainActivity that it should finish() too.
    // See onActivityResult() in MainActivity
    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }

    @Suppress("DEPRECATION")
    private fun allowDisplayOnLockScreen() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)

            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
    }

    override fun onDestroy() {
        unbindFromAlarmService()
        unregisterFromLocalAlarmBroadcastReceiver()
        super.onDestroy()
        logError("onDestroy")
    }
}