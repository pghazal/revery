package com.pghaz.revery.alarm

import android.os.Bundle
import com.pghaz.revery.BaseBottomSheetDialogFragment
import com.pghaz.revery.R
import com.pghaz.revery.model.app.alarm.Alarm
import com.pghaz.revery.util.Arguments
import io.github.kaaes.spotify.webapi.core.models.UserPrivate
import kotlinx.android.synthetic.main.fragment_alarm_more_options.*

class MoreOptionsAlarmFragment : BaseBottomSheetDialogFragment() {

    private var alarm: Alarm? = null
    private var spotifyUser: UserPrivate? = null

    override fun getLayoutResId(): Int {
        return R.layout.fragment_alarm_more_options
    }

    override fun parseArguments(arguments: Bundle?) {
        super.parseArguments(arguments)
        arguments?.let {
            alarm = it.getParcelable(Arguments.ARGS_ALARM)
            spotifyUser = it.getParcelable(Arguments.ARGS_SPOTIFY_USER)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(Arguments.ARGS_ALARM, alarm)
        outState.putParcelable(Arguments.ARGS_SPOTIFY_USER, spotifyUser)
        super.onSaveInstanceState(outState)
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        alarm?.let {
            shouldKeepPlayingSwitch.isChecked = it.metadata.shouldKeepPlaying

            spotifyUser?.let { user ->
                if (user.product == "premium") {
                    shuffleSwitch.isEnabled = true
                    shuffleSwitch.isChecked = it.metadata.shuffle
                } else {
                    shuffleSwitch.isEnabled = false
                }
            }
        }

        shouldKeepPlayingSwitch.setOnCheckedChangeListener { _, isChecked ->
            alarm?.metadata?.shouldKeepPlaying = isChecked
        }

        shuffleSwitch.setOnCheckedChangeListener { _, isChecked ->
            alarm?.metadata?.shuffle = isChecked
        }
    }

    companion object {

        const val TAG = "MoreOptionsAlarmFragment"

        fun newInstance(
            title: String?,
            alarm: Alarm?,
            spotifyUser: UserPrivate?
        ): MoreOptionsAlarmFragment {
            val fragment = MoreOptionsAlarmFragment()

            val args = Bundle()
            args.putInt(Arguments.ARGS_DIALOG_CLOSE_ICON, R.drawable.ic_validate)
            args.putString(Arguments.ARGS_DIALOG_TITLE, title)
            args.putParcelable(Arguments.ARGS_ALARM, alarm)
            args.putParcelable(Arguments.ARGS_SPOTIFY_USER, spotifyUser)

            fragment.arguments = args

            return fragment
        }
    }
}