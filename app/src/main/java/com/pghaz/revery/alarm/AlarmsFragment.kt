package com.pghaz.revery.alarm

import android.os.Bundle
import android.text.format.DateFormat
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.pghaz.revery.BaseFragment
import com.pghaz.revery.BuildConfig
import com.pghaz.revery.R
import com.pghaz.revery.adapter.ListItemDecoration
import com.pghaz.revery.adapter.alarm.AlarmsAdapter
import com.pghaz.revery.adapter.alarm.OnAlarmClickListener
import com.pghaz.revery.adapter.base.BaseAdapter
import com.pghaz.revery.model.app.Alarm
import com.pghaz.revery.model.app.BaseModel
import com.pghaz.revery.settings.SettingsFragment
import com.pghaz.revery.settings.SettingsHandler
import com.pghaz.revery.spotify.BaseSpotifyActivity
import com.pghaz.revery.viewmodel.alarm.AlarmsViewModel
import com.pghaz.spotify.webapi.auth.SpotifyAuthorizationClient
import kotlinx.android.synthetic.main.fragment_alarms.*

class AlarmsFragment : BaseFragment(), OnAlarmClickListener {

    private lateinit var alarmsViewModel: AlarmsViewModel
    private lateinit var alarmsAdapter: AlarmsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        alarmsAdapter = AlarmsAdapter(this, DateFormat.is24HourFormat(context))
        alarmsAdapter.onListChangedListener = object : BaseAdapter.OnListChangedListener {
            override fun onListChanged(
                previousList: MutableList<BaseModel>,
                currentList: MutableList<BaseModel>
            ) {
                // Scroll to new added item (except when app started: previousSize == 0)
                if (previousList.size > 0 && currentList.size > previousList.size) {
                    recyclerView.smoothScrollToPosition(alarmsAdapter.itemCount - 1)
                }

                if (currentList.size == 0) {
                    placeholder.visibility = View.VISIBLE
                } else {
                    placeholder.visibility = View.GONE
                }
            }
        }

        alarmsViewModel = ViewModelProvider(this).get(AlarmsViewModel::class.java)
        alarmsViewModel.alarmsLiveData.observe(this, { alarms ->
            alarmsAdapter.submitList(alarms)

            showAddAlarmButtonIfHidden()
        })
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_alarms
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        configureAlarmsList()
        configureAddAlarmButton()
    }

    private fun configureAlarmsList() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = alarmsAdapter
        recyclerView.setHasFixedSize(true)

        context?.let {
            val itemDecor = ListItemDecoration(ContextCompat.getColor(it, R.color.colorPrimary), 2)
            recyclerView.addItemDecoration(itemDecor)
        }
    }

    private fun configureAddAlarmButton() {
        addButton.setOnClickListener {
            var fragment =
                childFragmentManager.findFragmentByTag(CreateEditAlarmFragment.TAG) as CreateEditAlarmFragment?
            if (fragment == null) {
                fragment = CreateEditAlarmFragment.newInstance(getString(R.string.create_alarm))
                fragment.show(childFragmentManager, CreateEditAlarmFragment.TAG)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showAddAlarmButtonIfHidden()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (BuildConfig.DEBUG) {
            inflater.inflate(R.menu.menu_alarms_debug, menu)
        } else {
            inflater.inflate(R.menu.menu_alarms, menu)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_alarms_delete_all -> {
                deleteAllAlarms()
                showAddAlarmButtonIfHidden()
                true
            }
            R.id.menu_alarms_cancel_all -> {
                cancelAllAlarms()
                true
            }
            R.id.menu_settings -> {
                openSettings()
                true
            }
            R.id.menu_alarms_fire_alarm_spotify -> {
                AlarmHandler.fireAlarmNow(
                    context!!, 1,
                    recurring = false,
                    spotify = true,
                    fadeIn = true,
                    fadeInDuration = SettingsHandler.getFadeInDuration(context!!)
                )
                true
            }
            R.id.menu_alarms_fire_alarm_default -> {
                AlarmHandler.fireAlarmNow(
                    context!!, 1,
                    recurring = false,
                    spotify = false,
                    fadeIn = true,
                    fadeInDuration = SettingsHandler.getFadeInDuration(context!!)
                )
                true
            }
            R.id.menu_alarms_fire_alarm_default_recurring -> {
                AlarmHandler.fireAlarmNow(
                    context!!, 1,
                    recurring = true,
                    spotify = false,
                    fadeIn = true,
                    fadeInDuration = SettingsHandler.getFadeInDuration(context!!)
                )
                true
            }
            R.id.menu_spotify_clear_token -> {
                val spotifyAuthClient = SpotifyAuthorizationClient.Builder(
                    BuildConfig.SPOTIFY_CLIENT_ID,
                    BuildConfig.SPOTIFY_REDIRECT_URI
                ).build(context!!)
                spotifyAuthClient.setNeedsTokenRefresh(true)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAddAlarmButtonIfHidden() {
        if (addButton.isOrWillBeHidden) {
            addButton.show()
        }
    }

    private fun cancelAllAlarms() {
        val alarms = alarmsViewModel.alarmsLiveData.value
        alarms?.forEach {
            val alarm = Alarm(it)
            if (alarm.enabled) {
                alarmsViewModel.cancelAlarm(context, alarm)
                alarmsViewModel.update(alarm)
            }
        }
    }

    private fun deleteAllAlarms() {
        val alarms = alarmsViewModel.alarmsLiveData.value
        alarms?.forEach {
            alarmsViewModel.cancelAlarm(context, it)
            alarmsViewModel.delete(it)
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

    override fun onClick(alarm: Alarm) {
        var fragment =
            childFragmentManager.findFragmentByTag(CreateEditAlarmFragment.TAG) as CreateEditAlarmFragment?
        if (fragment == null) {
            if (activity is BaseSpotifyActivity) {
                fragment = CreateEditAlarmFragment.newInstance(
                    getString(R.string.edit_alarm),
                    alarm
                )
                fragment.show(childFragmentManager, CreateEditAlarmFragment.TAG)
            }
        }
    }

    override fun onToggle(alarm: Alarm) {
        // if alarm was enabled, we cancel it
        if (alarm.enabled) {
            alarmsViewModel.cancelAlarm(context, alarm)
        } else {
            alarmsViewModel.scheduleAlarm(context, alarm)
        }

        alarmsViewModel.update(alarm)
    }

    companion object {
        const val TAG = "ListAlarmsFragment"

        fun newInstance(): AlarmsFragment {
            val fragment = AlarmsFragment()

            val args = Bundle()
            fragment.arguments = args

            return fragment
        }
    }
}