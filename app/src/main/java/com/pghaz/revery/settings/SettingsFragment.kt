package com.pghaz.revery.settings

import android.os.Bundle
import com.pghaz.revery.BaseBottomSheetDialogFragment
import com.pghaz.revery.R
import com.pghaz.revery.util.Arguments

// TODO handle settings
class SettingsFragment : BaseBottomSheetDialogFragment() {
    override fun getLayoutResId(): Int {
        return R.layout.fragment_settings
    }

    override fun configureViews(savedInstanceState: Bundle?) {

    }

    companion object {
        const val TAG = "SettingsFragment"

        const val FADE_IN_DURATION: Long = 10000

        fun newInstance(title: String?): SettingsFragment {
            val fragment = SettingsFragment()

            val args = Bundle()
            args.putString(Arguments.ARGS_DIALOG_TITLE, title)

            fragment.arguments = args

            return fragment
        }
    }
}