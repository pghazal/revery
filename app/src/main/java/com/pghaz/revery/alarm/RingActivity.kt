package com.pghaz.revery.alarm

import android.app.Activity
import android.app.KeyguardManager
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.LiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ncorti.slidetoact.SlideToActView
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
import com.pghaz.revery.util.ViewUtils
import com.pghaz.revery.view.OnCustomTouchListener
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track
import kotlinx.android.synthetic.main.activity_ring.*
import java.util.*

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
    private var snoozeDurationIndex = 0

    private var mAlarmServiceBound: Boolean = false
    private var alarmLiveData: LiveData<Alarm>? = null
    private var playerStateSubscription: Subscription<PlayerState>? = null

    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            logError("onServiceConnected")
            mAlarmServiceBound = true

            val alarmService = service as AlarmService.AlarmServiceBinder
            alarmLiveData = service.getAlarmLiveData()

            observeAlarmUpdate(alarmService)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mAlarmServiceBound = false
            player = null
        }
    }

    private fun observeAlarmUpdate(service: AlarmService.AlarmServiceBinder) {
        alarmLiveData?.observe(this, { alarm ->
            player = service.getPlayer()

            configurePlayerControllers(alarm, player)
        })
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

    override fun onCreateAnimation() {
        super.onCreateAnimation()
        overridePendingTransition(0, 0)
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

    /*override fun onSaveInstanceState(outState: Bundle) {
        if (this::alarm.isInitialized) {
            IntentUtils.safePutAlarmIntoBundle(outState, alarm)
        }
        super.onSaveInstanceState(outState)
    }*/

    override fun parseArguments(args: Bundle?) {
        /*args?.let {
            if (it.containsKey(Arguments.ARGS_BUNDLE_ALARM)) {
                alarm = IntentUtils.safeGetAlarmFromBundle(it)
            }
        }*/
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        if (SettingsHandler.getSlideToTurnOff(this)) {
            slideTurnOffButton.visibility = View.VISIBLE
            turnOffButton.visibility = View.GONE
        } else {
            slideTurnOffButton.visibility = View.GONE
            turnOffButton.visibility = View.VISIBLE
        }

        slideTurnOffButton.onSlideCompleteListener =
            object : SlideToActView.OnSlideCompleteListener {
                override fun onSlideComplete(view: SlideToActView) {
                    broadcastStopAlarm()
                }
            }

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
            skipNext()
        }

        skipPreviousButton.setOnClickListener {
            skipPrevious()
        }

        gesturesInterceptorView.setOnTouchListener(object :
            OnCustomTouchListener(this@RingActivity) {
            override fun onSwipeLeft() {
                super.onSwipeLeft()
                skipPrevious()
            }

            override fun onSwipeRight() {
                super.onSwipeRight()
                skipNext()
            }

            override fun onDoubleTap() {
                super.onDoubleTap()

                if (SettingsHandler.isDoubleTapSnoozeEnabled(this@RingActivity)) {
                    broadcastSnooze(SnoozeDuration.values()[snoozeDurationIndex])
                }
            }
        })
    }

    private fun skipNext() {
        if (player is SpotifyPlayer) {
            (player as SpotifyPlayer?)?.getPlayerStateCallResult()
                ?.setResultCallback { playerState ->
                    if (playerState.playbackRestrictions.canSkipNext) {
                        (player as SpotifyPlayer?)?.skipNext()
                    }
                }
        }
    }

    private fun skipPrevious() {
        if (player is SpotifyPlayer) {
            (player as SpotifyPlayer?)?.getPlayerStateCallResult()
                ?.setResultCallback { playerState ->
                    if (playerState.playbackRestrictions.canSkipPrev) {
                        (player as SpotifyPlayer?)?.skipPrevious()
                    }
                }
        }
    }

    private fun configurePlayerControllers(alarm: Alarm, player: AbstractPlayer?) {
        if (player != null && player is SpotifyPlayer) {
            controllersContainer.visibility = View.VISIBLE

            player.playerConnectedLiveData.observe(this, { isPlayerConnected ->
                if (isPlayerConnected) {
                    unsubscribeFromPlayerState()
                    playerStateSubscription = player.getPlayerStateSubscription()

                    playerStateSubscription?.setEventCallback { playerState ->
                        val track: Track? = playerState.track

                        if (track != null) {
                            logError(track.name.toString() + " by " + track.artist.name)

                            if (playerState.isPaused) {
                                playPauseButton.setImageResource(R.drawable.selector_play)
                            } else {
                                playPauseButton.setImageResource(R.drawable.selector_pause)

                                titleTextView.text = track.name
                                artistNameTextView.text = track.artist.name
                                updateImageWithSpotify(player, playerState)
                            }

                            skipNextButton.isEnabled = playerState.playbackRestrictions.canSkipNext
                            skipPreviousButton.isEnabled =
                                playerState.playbackRestrictions.canSkipPrev
                        }
                    }
                } else {
                    unsubscribeFromPlayerState()
                }
            })
        } else {
            controllersContainer.visibility = View.GONE
            updateBackgroundImage(alarm.metadata.imageUrl)
            unsubscribeFromPlayerState()
        }
    }

    private fun unsubscribeFromPlayerState() {
        if (playerStateSubscription?.isCanceled == false) {
            playerStateSubscription?.cancel()
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
        alarmLiveData?.value?.let {
            val stopIntent =
                AlarmBroadcastReceiver.getStopAlarmActionIntent(applicationContext, it)
            sendBroadcast(stopIntent)
        }
    }

    private fun broadcastSnooze(snoozeDuration: SnoozeDuration) {
        alarmLiveData?.value?.let {
            val snoozeIntent =
                AlarmBroadcastReceiver.getSnoozeActionIntent(applicationContext, it)
            snoozeIntent.putExtra(Arguments.ARGS_SNOOZE_DURATION, snoozeDuration.minutes)
            sendBroadcast(snoozeIntent)
        }
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
        unsubscribeFromPlayerState()
        unbindFromAlarmService()
        unregisterFromLocalAlarmBroadcastReceiver()
        super.onDestroy()
        logError("onDestroy")
    }
}