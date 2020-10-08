package com.pghaz.revery

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.pghaz.revery.alarm.ListAlarmsFragment
import com.pghaz.revery.alarm.RingActivity
import com.pghaz.revery.service.AlarmService
import com.pghaz.revery.sleep.SleepFragment
import kotlinx.android.synthetic.main.activity_main.*

/**
 * MainActivity has launchMode = "singleTask" in Manifest
 */
class MainActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

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
        startRingActivityForResultAfterAlarmFires()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        // If the MainActivity is active (meaning instanciated background or foreground)
        // we receive the alarm intent in onNewIntent()
        // because launchMode in Manifest is "singleTask"
        startRingActivityForResultAfterAlarmFires()
    }

    private fun startRingActivityForResultAfterAlarmFires() {
        // If alarm is ringing then show RingActivity so that user can stop it
        if (AlarmService.isRunning) {
            val ringIntent = Intent(this, RingActivity::class.java)
            ringIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            ringIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            ringIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivityForResult(ringIntent, RingActivity.REQUEST_CODE_ALARM_RINGING)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RingActivity.REQUEST_CODE_ALARM_RINGING) {
            when (resultCode) {
                RESULT_OK -> {
                    // alarm has been stopped successfully: nothing else to do here
                }

                RESULT_CANCELED -> {
                    // We're coming back from RingActivity but user should first stop alarm before
                    // arriving on MainActivity
                    finish()
                }
            }
        }
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

    private fun selectNavigationItem(fragment: Fragment, tag: String) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment, tag)
            .commit()
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
}