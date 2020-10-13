package com.pghaz.revery.alarm

import android.app.Activity
import android.app.KeyguardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.WindowManager
import com.pghaz.revery.BaseActivity
import com.pghaz.revery.R
import com.pghaz.revery.alarm.model.app.Alarm
import com.pghaz.revery.alarm.service.AlarmService
import com.pghaz.revery.player.AbstractPlayer
import com.pghaz.revery.util.Arguments
import kotlinx.android.synthetic.main.activity_ring.*
import java.util.*

class RingActivity : BaseActivity() {

    companion object {
        private const val TAG = "RingActivity"

        const val REQUEST_CODE_ALARM_RINGING = 42
    }

    private var player: AbstractPlayer? = null

    private lateinit var alarm: Alarm

    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.e(TAG, "onServiceConnected")
            player = (service as AlarmService.AlarmServiceBinder).getService()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            player = null
        }
    }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        allowDisplayOnLockScreen()

        bindToAlarmService()
    }

    override fun parseArguments(args: Bundle?) {
        val alarmBundle = args?.getBundle(Arguments.ARGS_BUNDLE_ALARM)
        alarm = alarmBundle?.getParcelable<Alarm>(Arguments.ARGS_ALARM) as Alarm
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        timeTextView.text =
            String.format(Locale.getDefault(), "%02d:%02d", alarm.hour, alarm.minute)

        turnOffButton.setOnClickListener {
            stopAlarm(false)
        }

        snoozeButton.setOnClickListener {
            stopAlarm(true)
        }
    }

    private fun stopAlarm(snooze: Boolean) {
        player?.pause()

        unbindAlarmService()

        if (snooze) {
            // TODO show a notification when snoozed ?
            // TODO get snooze time from settings
            AlarmHandler.snooze(this, alarm, 2)
        }

        setResult(Activity.RESULT_OK)
        finish()
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

    private fun bindToAlarmService() {
        val service = Intent(applicationContext, AlarmService::class.java)
        bindService(service, mServiceConnection, Activity.BIND_AUTO_CREATE)
    }

    private fun unbindAlarmService() {
        unbindService(mServiceConnection)

        val service = Intent(applicationContext, AlarmService::class.java)
        applicationContext.stopService(service)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "onDestroy")
    }
}