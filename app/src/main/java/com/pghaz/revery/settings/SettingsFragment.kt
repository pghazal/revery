package com.pghaz.revery.settings

import android.os.Bundle
import com.pghaz.revery.BaseBottomSheetDialogFragment
import com.pghaz.revery.R

// TODO handle settings
class SettingsFragment : BaseBottomSheetDialogFragment() {
    override fun getLayoutResId(): Int {
        return R.layout.fragment_settings
    }

    override fun configureViews(savedInstanceState: Bundle?) {

    }

    companion object {
        const val TAG = "SettingsFragment"

        fun newInstance(title: String?): SettingsFragment {
            val fragment = SettingsFragment()

            val args = Bundle()
            args.putString(ARGS_DIALOG_TITLE, title)

            fragment.arguments = args

            return fragment
        }
    }
}