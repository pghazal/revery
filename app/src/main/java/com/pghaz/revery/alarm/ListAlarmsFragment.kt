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
import com.pghaz.revery.adapter.alarm.AlarmItemDecoration
import com.pghaz.revery.adapter.alarm.AlarmsAdapter
import com.pghaz.revery.adapter.alarm.OnAlarmClickListener
import com.pghaz.revery.adapter.base.BaseAdapter
import com.pghaz.revery.model.app.BaseModel
import com.pghaz.revery.model.app.alarm.Alarm
import com.pghaz.revery.settings.SettingsFragment
import com.pghaz.revery.spotify.BaseSpotifyActivity
import com.pghaz.revery.viewmodel.alarm.ListAlarmsViewModel
import com.pghaz.spotify.webapi.auth.SpotifyAuthorizationClient
import kotlinx.android.synthetic.main.fragment_list_alarms.*

class ListAlarmsFragment : BaseFragment(), OnAlarmClickListener {

    private lateinit var listAlarmsViewModel: ListAlarmsViewModel
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
                    placeholderListAlarms.visibility = View.VISIBLE
                } else {
                    placeholderListAlarms.visibility = View.GONE
                }
            }
        }

        listAlarmsViewModel = ViewModelProvider(this).get(ListAlarmsViewModel::class.java)
        listAlarmsViewModel.alarmsLiveData.observe(this, { alarms ->
            alarmsAdapter.submitList(alarms)

            showAddAlarmButtonIfHidden()
        })
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_list_alarms
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
            val itemDecor =
                AlarmItemDecoration(ContextCompat.getColor(it, R.color.colorPrimary), 2)
            recyclerView.addItemDecoration(itemDecor)
        }
    }

    private fun configureAddAlarmButton() {
        addAlarmButton.setOnClickListener {
            var createAlarmFragment =
                childFragmentManager.findFragmentByTag(CreateEditAlarmFragment.TAG) as CreateEditAlarmFragment?
            if (createAlarmFragment == null) {
                createAlarmFragment =
                    CreateEditAlarmFragment.newInstance(getString(R.string.create_alarm))
                createAlarmFragment.show(childFragmentManager, CreateEditAlarmFragment.TAG)
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
            R.id.menu_alarms_settings -> {
                openSettings()
                true
            }
            R.id.menu_alarms_fire_alarm_spotify -> {
                AlarmHandler.fireAlarmNow(
                    context, 1,
                    recurring = false,
                    spotify = true,
                    fadeIn = false,
                    // Useless for now
                    // fadeInDuration = SettingsHandler.getFadeInDuration(context!!)
                )
                true
            }
            R.id.menu_alarms_fire_alarm_default -> {
                AlarmHandler.fireAlarmNow(
                    context, 1,
                    recurring = false,
                    spotify = false,
                    fadeIn = true,
                    // Useless for now
                    // fadeInDuration = SettingsHandler.getFadeInDuration(context!!)
                )
                true
            }
            R.id.menu_alarms_fire_alarm_default_recurring -> {
                AlarmHandler.fireAlarmNow(
                    context, 1,
                    recurring = true,
                    spotify = false,
                    fadeIn = true,
                    // Useless for now
                    // fadeInDuration = SettingsHandler.getFadeInDuration(context!!)
                )
                true
            }
            R.id.menu_spotify_clear_token -> {
                context?.let {
                    val spotifyAuthClient = SpotifyAuthorizationClient.Builder(
                        BuildConfig.SPOTIFY_CLIENT_ID,
                        BuildConfig.SPOTIFY_REDIRECT_URI
                    ).build(it)
                    spotifyAuthClient.setNeedsTokenRefresh(true)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAddAlarmButtonIfHidden() {
        if (addAlarmButton.isOrWillBeHidden) {
            addAlarmButton.show()
        }
    }

    private fun cancelAllAlarms() {
        val alarms = listAlarmsViewModel.alarmsLiveData.value
        alarms?.forEach {
            val alarm = Alarm(it)
            if (alarm.enabled) {
                listAlarmsViewModel.cancelAlarm(context, alarm)
                listAlarmsViewModel.update(alarm)
            }
        }
    }

    private fun deleteAllAlarms() {
        val alarms = listAlarmsViewModel.alarmsLiveData.value
        alarms?.forEach {
            listAlarmsViewModel.cancelAlarm(context, it)
            listAlarmsViewModel.delete(it)
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
        var createAlarmFragment =
            childFragmentManager.findFragmentByTag(CreateEditAlarmFragment.TAG) as CreateEditAlarmFragment?
        if (createAlarmFragment == null) {
            if (activity is BaseSpotifyActivity) {
                createAlarmFragment = CreateEditAlarmFragment.newInstance(
                    getString(R.string.edit_alarm),
                    alarm
                )
                createAlarmFragment.show(childFragmentManager, CreateEditAlarmFragment.TAG)
            }
        }
    }

    override fun onToggle(alarm: Alarm) {
        // if alarm was enabled, we cancel it
        if (alarm.enabled) {
            listAlarmsViewModel.cancelAlarm(context, alarm)
        } else {
            listAlarmsViewModel.scheduleAlarm(context, alarm)
        }

        listAlarmsViewModel.update(alarm)
    }

    companion object {
        const val TAG = "ListAlarmsFragment"

        fun newInstance(): ListAlarmsFragment {
            val fragment = ListAlarmsFragment()

            val args = Bundle()
            fragment.arguments = args

            return fragment
        }
    }
}