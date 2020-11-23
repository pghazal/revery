package com.pghaz.revery.standby

import android.os.Bundle
import android.text.format.DateFormat
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.pghaz.revery.BaseFragment
import com.pghaz.revery.R
import com.pghaz.revery.settings.SettingsFragment
import kotlinx.android.synthetic.main.fragment_standby.*


class StandByFragment : BaseFragment() {

    override fun getLayoutResId(): Int {
        return R.layout.fragment_standby
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        val is24HourFormat = DateFormat.is24HourFormat(context)

        timePicker.setIs24HourView(is24HourFormat)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_standby, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                openSettings()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openSettings() {
        var fragment =
            childFragmentManager.findFragmentByTag(SettingsFragment.TAG) as SettingsFragment?
        if (fragment == null) {
            fragment = SettingsFragment.newInstance(getString(R.string.menu_settings))
            fragment.show(childFragmentManager, SettingsFragment.TAG)
        }
    }

    companion object {
        const val TAG = "StandByFragment"

        fun newInstance(): StandByFragment {
            val fragment = StandByFragment()

            val args = Bundle()
            fragment.arguments = args

            return fragment
        }
    }
}