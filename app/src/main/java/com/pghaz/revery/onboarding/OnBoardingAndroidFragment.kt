package com.pghaz.revery.onboarding

import com.pghaz.revery.battery.PowerSettingsFragment

class OnBoardingAndroidFragment : PowerSettingsFragment() {

    override fun isOpenedFromSettings(): Boolean {
        return false
    }
}