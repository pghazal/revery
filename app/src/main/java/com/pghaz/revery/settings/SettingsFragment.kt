package com.pghaz.revery.settings

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import com.pghaz.revery.BaseBottomSheetDialogFragment
import com.pghaz.revery.R
import com.pghaz.revery.util.Arguments
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : BaseBottomSheetDialogFragment() {
    override fun getLayoutResId(): Int {
        return R.layout.fragment_settings
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        // Snooze
        context?.let {
            val snoozeDurationPosition = SettingsHandler.getSnoozeDurationPosition(it)
            snoozeDurationSpinner.setSelection(snoozeDurationPosition)
        }

        snoozeDurationSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?,
                position: Int, id: Long
            ) {
                if (context == null) {
                    return
                }

                when (position) {
                    SnoozeDuration.FIVE_MINUTES.ordinal -> {
                        SettingsHandler.setSnoozeDuration(context!!, SnoozeDuration.FIVE_MINUTES)
                    }
                    SnoozeDuration.TEN_MINUTES.ordinal -> {
                        SettingsHandler.setSnoozeDuration(context!!, SnoozeDuration.TEN_MINUTES)
                    }
                    SnoozeDuration.FIFTEEN_MINUTES.ordinal -> {
                        SettingsHandler.setSnoozeDuration(context!!, SnoozeDuration.FIFTEEN_MINUTES)
                    }
                    SnoozeDuration.TWENTY_MINUTES.ordinal -> {
                        SettingsHandler.setSnoozeDuration(context!!, SnoozeDuration.TWENTY_MINUTES)
                    }
                    SnoozeDuration.THIRTY_MINUTES.ordinal -> {
                        SettingsHandler.setSnoozeDuration(context!!, SnoozeDuration.THIRTY_MINUTES)
                    }
                    SnoozeDuration.ONE_HOUR.ordinal -> {
                        SettingsHandler.setSnoozeDuration(context!!, SnoozeDuration.ONE_HOUR)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // do nothing
            }
        }

        // Fade In
        context?.let {
            val fadeInDurationPosition = SettingsHandler.getFadeInDurationPosition(it)
            fadeInDurationSpinner.setSelection(fadeInDurationPosition)
        }

        fadeInDurationSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?,
                position: Int, id: Long
            ) {
                if (context == null) {
                    return
                }

                when (position) {
                    FadeInDuration.FIVE_SECONDS.ordinal -> {
                        SettingsHandler.setFadeInDuration(context!!, FadeInDuration.FIVE_SECONDS)
                    }
                    FadeInDuration.TEN_SECONDS.ordinal -> {
                        SettingsHandler.setFadeInDuration(context!!, FadeInDuration.TEN_SECONDS)
                    }
                    FadeInDuration.TWENTY_SECONDS.ordinal -> {
                        SettingsHandler.setFadeInDuration(context!!, FadeInDuration.TWENTY_SECONDS)
                    }
                    FadeInDuration.THIRTY_SECONDS.ordinal -> {
                        SettingsHandler.setFadeInDuration(context!!, FadeInDuration.THIRTY_SECONDS)
                    }
                    FadeInDuration.ONE_MINUTE.ordinal -> {
                        SettingsHandler.setFadeInDuration(context!!, FadeInDuration.ONE_MINUTE)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // do nothing
            }
        }
    }

    companion object {
        const val TAG = "SettingsFragment"

        fun newInstance(title: String?): SettingsFragment {
            val fragment = SettingsFragment()

            val args = Bundle()
            args.putString(Arguments.ARGS_DIALOG_TITLE, title)

            fragment.arguments = args

            return fragment
        }
    }
}