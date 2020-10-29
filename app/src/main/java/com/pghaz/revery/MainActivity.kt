package com.pghaz.revery

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.pghaz.revery.alarm.ListAlarmsFragment
import com.pghaz.revery.alarm.RingActivity
import com.pghaz.revery.application.ReveryApplication
import com.pghaz.revery.extension.logError
import com.pghaz.revery.model.app.alarm.Alarm
import com.pghaz.revery.service.AlarmService
import com.pghaz.revery.sleep.SleepFragment
import com.pghaz.revery.util.Arguments
import com.pghaz.revery.util.IntentUtils
import kotlinx.android.synthetic.main.activity_main.*

/**
 * MainActivity has launchMode = "singleTask" in Manifest
 */
class MainActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private var mAlarmServiceBound: Boolean = false
    private lateinit var alarm: Alarm

    override fun getLayoutResId(): Int {
        return R.layout.activity_main
    }

    override fun shouldAnimateOnCreate(): Boolean {
        return true
    }

    override fun shouldAnimateOnFinish(): Boolean {
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindToAlarmServiceIfAlarmFired()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        // If the MainActivity is active (meaning instanciated background or foreground)
        // we receive the alarm intent in onNewIntent()
        // because launchMode in Manifest is "singleTask"
        bindToAlarmServiceIfAlarmFired()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RingActivity.REQUEST_CODE_ALARM_RINGING) {
            when (resultCode) {
                RESULT_OK -> {
                    // alarm has been stopped successfully: we unbind from service
                    unbindFromAlarmService()
                }

                RESULT_CANCELED -> {
                    // We're coming back from RingActivity but user should first stop alarm before
                    // arriving on MainActivity
                    finish()
                }
            }
        }
    }

    override fun parseArguments(args: Bundle?) {
        args?.let {
            val nullableAlarm = it.getParcelable(Arguments.ARGS_ALARM) as Alarm?
            nullableAlarm?.let { nonNullAlarm ->
                alarm = nonNullAlarm
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (this::alarm.isInitialized) {
            outState.putParcelable(Arguments.ARGS_ALARM, alarm)
        }
        super.onSaveInstanceState(outState)
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        configureBottomNavigationView(savedInstanceState)
    }

    private fun configureBottomNavigationView(savedInstanceState: Bundle?) {
        bottomNavigationView.setOnNavigationItemSelectedListener(this)

        // If we're being restored from a previous state,
        // then we don't need to do anything and should return or else
        // we could end up with overlapping fragments.
        if (savedInstanceState != null) {
            return
        }

        // Select the first tab (alarms) which will call the listener as it is already set
        bottomNavigationView.selectedItemId = R.id.alarm_tab
    }

    override fun onResume() {
        super.onResume()
        if (!ReveryApplication.isNotificationEnabled(this)) {
            showNotificationDisabledDialog()
        }
    }

    private fun showNotificationDisabledDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_notification_disabled, null)
        val goToSettingsButton = view.findViewById<AppCompatButton>(R.id.goToSettingsButton)

        val dialog = AlertDialog.Builder(this).apply {
            setCancelable(true)
            setView(view)
        }.create()
        dialog.show()

        goToSettingsButton.setOnClickListener {
            dialog.dismiss()
            ReveryApplication.openAppNotificationSettings(this@MainActivity)
        }
    }

    private fun selectNavigationItem(fragment: Fragment, tag: String) {
        replaceFragment(fragment, tag)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.alarm_tab -> {
                openAlarmsTab()
                true
            }

            R.id.sleep_tab -> {
                openSleepTab()
                true
            }

            else -> false
        }
    }

    private fun openAlarmsTab() {
        // Check first if the fragment already exists
        var alarmsFragment =
            supportFragmentManager.findFragmentByTag(ListAlarmsFragment.TAG) as ListAlarmsFragment?
        // If it doesn't, create it
        if (alarmsFragment == null) {
            alarmsFragment = ListAlarmsFragment.newInstance()
        }
        selectNavigationItem(alarmsFragment, ListAlarmsFragment.TAG)
    }

    private fun openSleepTab() {
        // Check first if the fragment already exists
        var sleepFragment =
            supportFragmentManager.findFragmentByTag(SleepFragment.TAG) as SleepFragment?
        // If it doesn't, create it
        if (sleepFragment == null) {
            sleepFragment = SleepFragment.newInstance()
        }
        selectNavigationItem(sleepFragment, SleepFragment.TAG)
    }

    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            logError("onServiceConnected")
            mAlarmServiceBound = true

            alarm = (service as AlarmService.AlarmServiceBinder).getAlarm()

            startRingActivityForResult(alarm)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mAlarmServiceBound = false
        }
    }

    private fun bindToAlarmServiceIfAlarmFired() {
        // If alarm is ringing then bind to AlarmService to get Alarm
        if (AlarmService.isRunning) {
            if (!mAlarmServiceBound) {
                val service = Intent(applicationContext, AlarmService::class.java)
                bindService(service, mServiceConnection, Activity.BIND_AUTO_CREATE)
            } else {
                startRingActivityForResult(alarm)
            }
        }
    }

    private fun unbindFromAlarmService() {
        if (mAlarmServiceBound) {
            mAlarmServiceBound = false
            unbindService(mServiceConnection)
        }
    }

    private fun startRingActivityForResult(alarm: Alarm) {
        val ringIntent = Intent(this@MainActivity, RingActivity::class.java)
        ringIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        ringIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        ringIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        IntentUtils.safePutAlarmIntoIntent(ringIntent, alarm)

        startActivityForResult(ringIntent, RingActivity.REQUEST_CODE_ALARM_RINGING)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindFromAlarmService()
    }
}