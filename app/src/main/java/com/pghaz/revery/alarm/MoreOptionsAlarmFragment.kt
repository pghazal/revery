package com.pghaz.revery.alarm

import android.os.Bundle
import com.pghaz.revery.BaseBottomSheetDialogFragment
import com.pghaz.revery.R
import com.pghaz.revery.util.Arguments

class MoreOptionsAlarmFragment : BaseBottomSheetDialogFragment() {

    override fun getLayoutResId(): Int {
        return R.layout.fragment_alarm_more_options
    }

    override fun configureViews(savedInstanceState: Bundle?) {

    }

    companion object {

        const val TAG = "MoreOptionsAlarmFragment"

        fun newInstance(title: String?): MoreOptionsAlarmFragment {
            val fragment = MoreOptionsAlarmFragment()

            val args = Bundle()
            args.putString(Arguments.ARGS_DIALOG_TITLE, title)

            fragment.arguments = args

            return fragment
        }
    }
}