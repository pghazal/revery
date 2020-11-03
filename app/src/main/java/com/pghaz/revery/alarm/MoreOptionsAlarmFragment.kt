package com.pghaz.revery.alarm

import android.os.Bundle
import com.pghaz.revery.BaseBottomSheetDialogFragment
import com.pghaz.revery.R
import com.pghaz.revery.model.app.alarm.Alarm
import com.pghaz.revery.spotify.BaseSpotifyActivity
import com.pghaz.revery.util.Arguments
import com.spotify.protocol.types.Repeat
import kotlinx.android.synthetic.main.fragment_alarm_more_options.*

class MoreOptionsAlarmFragment : BaseBottomSheetDialogFragment() {

    private var alarm: Alarm? = null

    override fun getLayoutResId(): Int {
        return R.layout.fragment_alarm_more_options
    }

    override fun parseArguments(arguments: Bundle?) {
        super.parseArguments(arguments)
        arguments?.let {
            alarm = it.getParcelable(Arguments.ARGS_ALARM)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(Arguments.ARGS_ALARM, alarm)
        super.onSaveInstanceState(outState)
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        if (activity is BaseSpotifyActivity) {
            alarm?.let {
                shouldKeepPlayingSwitch.isChecked = it.metadata.shouldKeepPlaying

                shouldKeepPlayingSwitch.setOnCheckedChangeListener { _, isChecked ->
                    it.metadata.shouldKeepPlaying = isChecked
                }

                (activity as BaseSpotifyActivity?)?.spotifyAuthClient?.getCurrentUser()
                    ?.let { user ->
                        if (user.product == "premium") {
                            repeatToggle.isEnabled = true
                            shuffleToggle.isEnabled = true
                            shuffleToggle.isChecked = it.metadata.shuffle
                        } else {
                            repeatToggle.isEnabled = false
                            shuffleToggle.isEnabled = false
                            shuffleToggle.isChecked = false
                        }
                    }

                shuffleToggle.setOnCheckedChangeListener { _, isChecked ->
                    it.metadata.shuffle = isChecked
                }

                var repeatMode = it.metadata.repeat
                updateRepeatToggle(repeatMode)

                repeatToggle.setOnClickListener { _ ->
                    repeatMode = when (repeatMode) {
                        Repeat.OFF -> {
                            Repeat.ONE
                        }
                        Repeat.ONE -> {
                            Repeat.ALL
                        }
                        else -> {
                            Repeat.OFF
                        }
                    }

                    updateRepeatToggle(repeatMode)

                    it.metadata.repeat = repeatMode
                }
            }
        }
    }

    private fun updateRepeatToggle(repeatMode: Int) {
        when (repeatMode) {
            Repeat.OFF -> {
                repeatToggle.isChecked = false
                repeatToggle.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_repeat_off,
                    0,
                    0,
                    0
                )
            }
            Repeat.ONE -> {
                repeatToggle.isChecked = true
                repeatToggle.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_repeat_one,
                    0,
                    0,
                    0
                )
            }
            Repeat.ALL -> {
                repeatToggle.isChecked = true
                repeatToggle.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_repeat_all,
                    0,
                    0,
                    0
                )
            }
        }
    }

    companion object {

        const val TAG = "MoreOptionsAlarmFragment"

        fun newInstance(
            title: String?,
            alarm: Alarm?
        ): MoreOptionsAlarmFragment {
            val fragment = MoreOptionsAlarmFragment()

            val args = Bundle()
            args.putInt(Arguments.ARGS_DIALOG_CLOSE_ICON, R.drawable.ic_validate)
            args.putString(Arguments.ARGS_DIALOG_TITLE, title)
            args.putParcelable(Arguments.ARGS_ALARM, alarm)

            fragment.arguments = args

            return fragment
        }
    }
}