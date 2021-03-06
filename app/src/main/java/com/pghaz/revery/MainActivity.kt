package com.pghaz.revery

import android.annotation.SuppressLint
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
import com.pghaz.revery.alarm.AlarmsFragment
import com.pghaz.revery.alarm.RingActivity
import com.pghaz.revery.extension.logError
import com.pghaz.revery.notification.NotificationHandler
import com.pghaz.revery.onboarding.OnBoardingActivity
import com.pghaz.revery.service.AlarmService
import com.pghaz.revery.service.RescheduleAlarmsService
import com.pghaz.revery.settings.SettingsHandler
import com.pghaz.revery.settings.TabFeature
import com.pghaz.revery.spotify.BaseSpotifyActivity
import com.pghaz.revery.standby.StandByFragment
import com.pghaz.revery.timer.TimersFragment
import com.pghaz.revery.util.Arguments
import kotlinx.android.synthetic.main.activity_main.*

/**
 * MainActivity has launchMode = "singleTask" in Manifest
 */
class MainActivity : BaseSpotifyActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private var notificationsDisabledDialog: AlertDialog? = null

    private var mAlarmServiceBound: Boolean = false

    private lateinit var lastOpenedTabFeature: TabFeature

    override fun getLayoutResId(): Int {
        return R.layout.activity_main
    }

    override fun onCreateAnimation() {
        super.onCreateAnimation()
        overridePendingTransition(0, 0)
    }

    override fun onFinishAnimation() {
        super.onFinishAnimation()
        overridePendingTransition(0, 0)
    }

    override fun shouldShowAuth(): Boolean {
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindToAlarmServiceIfAlarmFired()

        NotificationHandler.cancel(this, NotificationHandler.NOTIFICATION_ID_RESCHEDULE)
        NotificationHandler.cancel(this, NotificationHandler.NOTIFICATION_ID_ERROR_OCCURRED)
        RescheduleAlarmsService.rescheduleEnabledAlarms(application, this)

        if (!SettingsHandler.getOnBoardingShown(this)) {
            // Show on boarding
            startActivity(Intent(this, OnBoardingActivity::class.java))
        }
    }

    override fun onSpotifyAuthorizedAndAvailable() {
        // do nothing for now
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
        val settingsLastOpenedTab = SettingsHandler.getLastOpenedTab(this)

        val tabFeatureOrdinal = args?.let {
            it.getInt(Arguments.NOTIFICATION_SOURCE, settingsLastOpenedTab.ordinal)
        } ?: kotlin.run {
            settingsLastOpenedTab.ordinal
        }

        lastOpenedTabFeature = TabFeature.values()[tabFeatureOrdinal]
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        super.configureViews(savedInstanceState)
        buildNotificationsDisabledDialog()

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

        // Select the last opened tab which will call the listener as it is already set
        when (lastOpenedTabFeature) {
            TabFeature.ALARM -> {
                bottomNavigationView.selectedItemId = R.id.alarm_tab
            }

            TabFeature.TIMER -> {
                bottomNavigationView.selectedItemId = R.id.timer_tab
            }

            TabFeature.STANDBY -> {
                bottomNavigationView.selectedItemId = R.id.standby_tab
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!NotificationHandler.areNeededNotificationsEnabled(this)) {
            showNotificationsDisabledDialog()
        }
    }

    private fun showNotificationsDisabledDialog() {
        if (!notificationsDisabledDialog?.isShowing!!) {
            notificationsDisabledDialog?.show()
        }
    }

    @SuppressLint("InflateParams")
    private fun buildNotificationsDisabledDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_notification_disabled, null)
        val goToSettingsButton = view.findViewById<AppCompatButton>(R.id.goToSettingsButton)

        notificationsDisabledDialog = AlertDialog.Builder(this).apply {
            setCancelable(true)
            setView(view)
        }.create()

        goToSettingsButton.setOnClickListener {
            notificationsDisabledDialog?.dismiss()
            NotificationHandler.openAppNotificationSettings(this@MainActivity)
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

            R.id.timer_tab -> {
                openTimersTab()
                true
            }

            R.id.standby_tab -> {
                openStandByTab()
                true
            }

            else -> false
        }
    }

    private fun openAlarmsTab() {
        SettingsHandler.setLastOpenedTab(this, TabFeature.ALARM)
        // Check first if the fragment already exists
        var fragment =
            supportFragmentManager.findFragmentByTag(AlarmsFragment.TAG) as AlarmsFragment?
        // If it doesn't, create it
        if (fragment == null) {
            fragment = AlarmsFragment.newInstance()
        }
        selectNavigationItem(fragment, AlarmsFragment.TAG)
    }

    private fun openTimersTab() {
        SettingsHandler.setLastOpenedTab(this, TabFeature.TIMER)
        // Check first if the fragment already exists
        var fragment =
            supportFragmentManager.findFragmentByTag(TimersFragment.TAG) as TimersFragment?
        // If it doesn't, create it
        if (fragment == null) {
            fragment = TimersFragment.newInstance()
        }
        selectNavigationItem(fragment, TimersFragment.TAG)
    }

    private fun openStandByTab() {
        SettingsHandler.setLastOpenedTab(this, TabFeature.STANDBY)
        // Check first if the fragment already exists
        var fragment =
            supportFragmentManager.findFragmentByTag(StandByFragment.TAG) as StandByFragment?
        // If it doesn't, create it
        if (fragment == null) {
            fragment = StandByFragment.newInstance()
        }
        selectNavigationItem(fragment, StandByFragment.TAG)
    }

    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            logError("onServiceConnected")
            mAlarmServiceBound = true

            startRingActivityForResult()
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
                startRingActivityForResult()
            }
        }
    }

    private fun unbindFromAlarmService() {
        if (mAlarmServiceBound) {
            mAlarmServiceBound = false
            unbindService(mServiceConnection)
        }
    }

    private fun startRingActivityForResult() {
        val ringIntent = Intent(this@MainActivity, RingActivity::class.java)
        ringIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        ringIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        ringIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        startActivityForResult(ringIntent, RingActivity.REQUEST_CODE_ALARM_RINGING)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindFromAlarmService()
    }

    override fun onAuthorizationCancelled() {
        // do nothing
    }

    override fun onAuthorizationFailed(error: String?) {
        // do nothing
    }

    override fun onAuthorizationRefused(error: String?) {
        // do nothing
    }
}