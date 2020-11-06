package com.pghaz.revery.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class OnBoardingAdapter(fragmentActivity: FragmentActivity, private val onBoardingPagesNumber: Int) :
    FragmentStateAdapter(fragmentActivity) {

    companion object {
        const val DEFAULT_ON_BOARDING_PAGE_NUMBER = 4
    }

    override fun getItemCount(): Int = onBoardingPagesNumber

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OnBoardingIntroFragment()
            1 -> OnBoardingSettingsFragment()
            2 -> OnBoardingSpotifyFragment()
            3 -> OnBoardingAndroidFragment()
            else -> {
                OnBoardingIntroFragment()
            }
        }
    }
}