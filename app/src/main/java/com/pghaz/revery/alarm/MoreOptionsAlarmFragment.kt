package com.pghaz.revery.alarm

import android.os.Bundle
import com.pghaz.revery.BaseBottomSheetDialogFragment
import com.pghaz.revery.R
import com.pghaz.revery.alarm.model.app.Alarm
import com.pghaz.revery.util.Arguments
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
        alarm?.let {
            shouldKeepPlayingSwitch.isChecked = it.metadata.shouldKeepPlaying
            shuffleSwitch.isChecked = it.metadata.shuffle
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

        fun newInstance(title: String?, alarm: Alarm?): MoreOptionsAlarmFragment {
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