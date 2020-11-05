package com.pghaz.revery.sleep

import android.os.Bundle
import com.pghaz.revery.BaseFragment
import com.pghaz.revery.R

class SleepFragment : BaseFragment() {

    override fun getLayoutResId(): Int {
        return R.layout.fragment_sleep
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun configureViews(savedInstanceState: Bundle?) {

    }

    companion object {
        const val TAG = "SleepFragment"

        fun newInstance(): SleepFragment {
            val fragment = SleepFragment()

            val args = Bundle()
            fragment.arguments = args

            return fragment
        }

    }
}