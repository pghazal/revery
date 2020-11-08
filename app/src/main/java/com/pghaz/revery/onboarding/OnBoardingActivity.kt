package com.pghaz.revery.onboarding

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.pghaz.revery.R
import com.pghaz.revery.battery.PowerManagerHandler
import com.pghaz.revery.notification.NotificationHandler
import com.pghaz.revery.settings.SettingsHandler
import com.pghaz.revery.spotify.BaseSpotifyActivity
import kotlinx.android.synthetic.main.activity_on_boarding.*


class OnBoardingActivity : BaseSpotifyActivity() {

    private var currentPosition: Int = 0
    private var pagesNumber: Int = OnBoardingAdapter.DEFAULT_ON_BOARDING_PAGE_NUMBER

    override fun onCreateAnimation() {
        super.onCreateAnimation()
        overridePendingTransition(0, 0)
    }

    override fun onFinishAnimation() {
        super.onFinishAnimation()
        overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave)
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_on_boarding
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val isNotificationPolicyAccessGranted =
            NotificationHandler.isNotificationPolicyAccessGranted(notificationManager)
        val isIgnoringBatteryOptimizations =
            PowerManagerHandler.isIgnoringBatteryOptimizations(this)
        val autoStartIntent = PowerManagerHandler.getAutoStartFeatureIntent(this)
        val hasAutoStartFeature = autoStartIntent != null

        pagesNumber =
            if (isNotificationPolicyAccessGranted && !hasAutoStartFeature && isIgnoringBatteryOptimizations) {
                OnBoardingAdapter.DEFAULT_ON_BOARDING_PAGE_NUMBER - 1
            } else {
                OnBoardingAdapter.DEFAULT_ON_BOARDING_PAGE_NUMBER
            }

        viewPager.adapter = OnBoardingAdapter(this, pagesNumber)
        viewPager.registerOnPageChangeCallback(onPageChangeCallback)

        TabLayoutMediator(dotsIndicatorsView, viewPager) { _, _ ->
        }.attach()

        nextButton.setOnClickListener {
            if (currentPosition == pagesNumber - 1) {
                finish()
            } else {
                viewPager.setCurrentItem(currentPosition + 1, true)
            }
        }
    }

    override fun shouldShowAuth(): Boolean {
        return false
    }

    override fun onSpotifyAuthorizedAndAvailable() {

    }

    override fun parseArguments(args: Bundle?) {
        // do nothing
    }

    override fun onBackPressed() {
        when (currentPosition) {
            (pagesNumber - 1) -> {
                super.onBackPressed()
            }
            else -> {
                viewPager.setCurrentItem(currentPosition + 1, true)
            }
        }
    }

    override fun finish() {
        SettingsHandler.setOnBoardingShown(this, true)
        super.finish()
    }

    private var onPageChangeCallback: ViewPager2.OnPageChangeCallback = object :
        ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            currentPosition = position

            when (position) {
                (pagesNumber - 1) -> {
                    nextButton.text = getString(R.string.lets_go)
                }
                else -> {
                    nextButton.text = getString(R.string.next)
                }
            }
        }
    }

    override fun onAuthorizationCancelled() {

    }

    override fun onAuthorizationFailed(error: String?) {

    }

    override fun onAuthorizationRefused(error: String?) {

    }
}