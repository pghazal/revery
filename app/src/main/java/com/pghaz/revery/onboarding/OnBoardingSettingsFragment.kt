package com.pghaz.revery.onboarding

import android.os.Bundle
import android.view.View
import com.pghaz.revery.BaseFragment
import com.pghaz.revery.R
import com.pghaz.revery.settings.SettingsHandler
import kotlinx.android.synthetic.main.fragment_on_boarding_settings.*
import java.util.*

class OnBoardingSettingsFragment : BaseFragment() {

    private var snoozeDurationIndex = 0

    override fun getLayoutResId(): Int {
        return R.layout.fragment_on_boarding_settings
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        // Slide to turn off
        context?.let {
            val slideToTurnOffEnabled = SettingsHandler.getSlideToTurnOff(it)
            slideToTurnOffSwitch.isChecked = slideToTurnOffEnabled
            updateTurnOffButton(slideToTurnOffEnabled)
        }
        slideToTurnOffSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            SettingsHandler.setSlideToTurnOff(buttonView.context, isChecked)
            updateTurnOffButton(isChecked)
        }

        // Can change snooze duration
        context?.let {
            val canChangeSnoozeDuration = SettingsHandler.getCanChangeSnoozeDuration(it)
            canChangeSnoozeDurationSwitch.isChecked = canChangeSnoozeDuration
            updateSnoozeButtons(canChangeSnoozeDuration)

            val snoozeDurationArray = resources.getStringArray(R.array.snooze_duration_array)
            snoozeDurationIndex = SettingsHandler.getSnoozeDurationPosition(it)

            minusSnoozeButton.setOnClickListener {
                snoozeDurationIndex -= 1
                if (snoozeDurationIndex <= 0) {
                    snoozeDurationIndex = 0
                }
                updateSnoozeText(snoozeDurationArray)
            }

            plusSnoozeButton.setOnClickListener {
                snoozeDurationIndex += 1
                if (snoozeDurationIndex >= snoozeDurationArray.size) {
                    snoozeDurationIndex = snoozeDurationArray.size - 1
                }
                updateSnoozeText(snoozeDurationArray)
            }

            updateSnoozeText(snoozeDurationArray)
        }
        canChangeSnoozeDurationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            SettingsHandler.setCanChangeSnoozeDuration(buttonView.context, isChecked)
            updateSnoozeButtons(isChecked)
        }
    }

    private fun updateTurnOffButton(isChecked: Boolean) {
        if (isChecked) {
            slideTurnOffButton.visibility = View.VISIBLE
            turnOffButton.visibility = View.GONE
        } else {
            slideTurnOffButton.visibility = View.GONE
            turnOffButton.visibility = View.VISIBLE
        }
    }

    private fun updateSnoozeButtons(isChecked: Boolean) {
        if (isChecked) {
            minusSnoozeButton.visibility = View.VISIBLE
            plusSnoozeButton.visibility = View.VISIBLE
        } else {
            snoozeDurationIndex = 0
            minusSnoozeButton.visibility = View.GONE
            plusSnoozeButton.visibility = View.GONE
        }
    }

    private fun updateSnoozeText(snoozeDurationArray: Array<String>) {
        snoozeButton.text =
            String.format(
                Locale.getDefault(),
                "%s\n%s",
                getString(R.string.alarm_snooze),
                snoozeDurationArray[snoozeDurationIndex]
            )
    }
}