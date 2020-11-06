package com.pghaz.revery.battery

import android.os.Bundle
import com.pghaz.revery.BaseActivity
import com.pghaz.revery.R

class PowerSettingsActivity : BaseActivity() {

    override fun getLayoutResId(): Int {
        return R.layout.activity_power_settings
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        // Check first if the fragment already exists
        var fragment =
            supportFragmentManager.findFragmentByTag(PowerSettingsFragment.TAG) as PowerSettingsFragment?
        // If it doesn't, create it
        if (fragment == null) {
            fragment = PowerSettingsFragment()
        }
        replaceFragment(fragment, PowerSettingsFragment.TAG)
    }

    override fun parseArguments(args: Bundle?) {

    }
}