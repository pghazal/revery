package com.pghaz.revery.alarm

import android.app.Activity
import android.app.KeyguardManager
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.WindowManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.pghaz.revery.BaseActivity
import com.pghaz.revery.R
import com.pghaz.revery.alarm.broadcastreceiver.AlarmBroadcastReceiver
import com.pghaz.revery.alarm.model.app.AbstractAlarm
import com.pghaz.revery.alarm.service.AlarmService
import com.pghaz.revery.extension.logError
import com.pghaz.revery.player.AbstractPlayer
import com.pghaz.revery.settings.SettingsHandler
import com.pghaz.revery.util.IntentUtils
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

    private lateinit var alarm: AbstractAlarm

    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            logError("onServiceConnected")
            player = (service as AlarmService.AlarmServiceBinder).getService()
        }

        override fun onServiceDisconnected(name: ComponentName) {
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

    private fun isServiceBound(): Boolean {
        return player != null
    }

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
        unbindService(mServiceConnection)
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
        turnOffButton.setOnClickListener {
            broadcastStopAlarm()
        }

        val snoozeDurationArray = resources.getStringArray(R.array.snooze_duration_array)
        snoozeButton.text =
            String.format(
                Locale.getDefault(),
                "%s\n%s",
                getString(R.string.alarm_snooze),
                snoozeDurationArray[SettingsHandler.getSnoozeDurationPosition(this)]
            )

        snoozeButton.setOnClickListener {
            broadcastSnooze()
        }
    }

    private fun broadcastStopAlarm() {
        val stopIntent = AlarmBroadcastReceiver.getStopAlarmActionIntent(applicationContext, alarm)
        sendBroadcast(stopIntent)
    }

    private fun broadcastSnooze() {
        val snoozeIntent = AlarmBroadcastReceiver.getSnoozeActionIntent(applicationContext, alarm)
        sendBroadcast(snoozeIntent)
    }

    // If this activity exists, it means an alarm is ringing.
    // By setting result RESULT_CANCELED, we say to the MainActivity that it should finish() too.
    // See onActivityResult() in MainActivity
    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }

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