package com.pghaz.revery.timer

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.pghaz.revery.BaseFragment
import com.pghaz.revery.R
import com.pghaz.revery.adapter.ListItemDecoration
import com.pghaz.revery.adapter.base.BaseAdapter
import com.pghaz.revery.adapter.timer.OnTimerClickListener
import com.pghaz.revery.adapter.timer.TimersAdapter
import com.pghaz.revery.model.app.BaseModel
import com.pghaz.revery.model.app.Timer
import com.pghaz.revery.model.app.TimerState
import com.pghaz.revery.settings.SettingsFragment
import com.pghaz.revery.spotify.BaseSpotifyActivity
import com.pghaz.revery.viewmodel.timer.TimersViewModel
import kotlinx.android.synthetic.main.fragment_timers.*

class TimersFragment : BaseFragment(), OnTimerClickListener {

    private lateinit var timersViewModel: TimersViewModel
    private lateinit var timersAdapter: TimersAdapter

    override fun getLayoutResId(): Int {
        return R.layout.fragment_timers
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        timersAdapter = TimersAdapter(this)
        timersAdapter.onListChangedListener = object : BaseAdapter.OnListChangedListener {
            override fun onListChanged(
                previousList: MutableList<BaseModel>,
                currentList: MutableList<BaseModel>
            ) {
                // Scroll to new added item (except when app started: previousSize == 0)
                if (previousList.size > 0 && currentList.size > previousList.size) {
                    recyclerView.smoothScrollToPosition(timersAdapter.itemCount - 1)
                }

                if (currentList.size == 0) {
                    placeholder.visibility = View.VISIBLE
                } else {
                    placeholder.visibility = View.GONE
                }
            }
        }

        timersViewModel = ViewModelProvider(this).get(TimersViewModel::class.java)
        timersViewModel.timersLiveData.observe(this, { timers ->
            timersAdapter.submitList(timers)

            showAddTimerButtonIfHidden()
        })
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        configureTimersList()
        configureAddTimerButton()
    }

    private fun configureTimersList() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = timersAdapter
        recyclerView.setHasFixedSize(true)

        context?.let {
            val itemDecor = ListItemDecoration(ContextCompat.getColor(it, R.color.colorPrimary), 2)
            recyclerView.addItemDecoration(itemDecor)
        }
    }

    private fun configureAddTimerButton() {
        addButton.setOnClickListener {
            var fragment =
                childFragmentManager.findFragmentByTag(CreateEditTimerFragment.TAG) as CreateEditTimerFragment?
            if (fragment == null) {
                fragment =
                    CreateEditTimerFragment.newInstance(getString(R.string.create_timer))
                fragment.show(childFragmentManager, CreateEditTimerFragment.TAG)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_timers, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_timers_cancel_all -> {
                cancelAllTimers()
                true
            }
            R.id.menu_settings -> {
                openSettings()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun cancelAllTimers() {
        val timers = timersViewModel.timersLiveData.value
        timers?.forEach {
            val timer = Timer(it)
            if (timer.state == TimerState.RUNNING && context != null) {
                timersViewModel.stopTimer(context!!, timer)
                timersViewModel.resetTimer(context!!, timer)
                timersViewModel.update(timer)
            }
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

    private fun showAddTimerButtonIfHidden() {
        if (addButton.isOrWillBeHidden) {
            addButton.show()
        }
    }

    override fun onTimerClicked(timer: Timer) {
        if (timer.state == TimerState.CREATED) {
            var fragment =
                childFragmentManager.findFragmentByTag(CreateEditTimerFragment.TAG) as CreateEditTimerFragment?
            if (fragment == null) {
                if (activity is BaseSpotifyActivity) {
                    fragment = CreateEditTimerFragment.newInstance(
                        getString(R.string.edit_timer),
                        timer
                    )
                    fragment.show(childFragmentManager, CreateEditTimerFragment.TAG)
                }
            }
        } else {
            Toast.makeText(context, getString(R.string.timer_in_use), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPlayPauseButtonClicked(timer: Timer) {
        context?.let {
            when (timer.state) {
                TimerState.CREATED,
                TimerState.PAUSED -> {
                    timersViewModel.startTimer(it, timer)
                }

                TimerState.RUNNING -> {
                    timersViewModel.pauseTimer(it, timer)
                }

                TimerState.RINGING -> {
                    timersViewModel.stopTimer(it, timer)
                }
            }

            timersViewModel.update(timer)
        }
    }

    override fun onResetButtonClicked(timer: Timer) {
        context?.let {
            timersViewModel.resetTimer(it, timer)
            timersViewModel.update(timer)
        }
    }

    override fun onIncrementButtonClicked(timer: Timer, incrementValue: Int) {
        context?.let {
            timersViewModel.incrementTimer(it, timer, incrementValue)
            timersViewModel.update(timer)
        }
    }

    override fun onResume() {
        super.onResume()
        showAddTimerButtonIfHidden()
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