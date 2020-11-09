package com.pghaz.revery.timer

import android.os.Bundle
import com.pghaz.revery.BaseFragment
import com.pghaz.revery.R

class TimersFragment : BaseFragment() {

    override fun getLayoutResId(): Int {
        return R.layout.fragment_timers
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun configureViews(savedInstanceState: Bundle?) {

    }

    companion object {
        const val TAG = "TimersFragment"

        fun newInstance(): TimersFragment {
            val fragment = TimersFragment()

            val args = Bundle()
            fragment.arguments = args

            return fragment
        }

    }
}