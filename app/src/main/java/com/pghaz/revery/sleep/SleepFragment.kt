package com.pghaz.revery.sleep

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pghaz.revery.R

class SleepFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sleep, container, false)
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