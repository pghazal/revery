package com.pghaz.revery.alarm

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import com.pghaz.revery.BaseActivity
import com.pghaz.revery.R
import com.pghaz.revery.service.AlarmService
import kotlinx.android.synthetic.main.activity_ring.*

class RingActivity : BaseActivity() {

    override fun getLayoutResId(): Int {
        return R.layout.activity_ring
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        stopAlarmButton.setOnClickListener {
            val intentService = Intent(applicationContext, AlarmService::class.java)
            applicationContext.stopService(intentService)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        allowDisplayOnLockScreen()
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
}