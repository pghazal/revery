package com.pghaz.revery.alarm

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.pghaz.revery.BaseFragment
import com.pghaz.revery.R
import com.pghaz.revery.alarm.adapter.AlarmItemDecoration
import com.pghaz.revery.alarm.adapter.AlarmsAdapter
import com.pghaz.revery.alarm.adapter.OnAlarmClickListener
import com.pghaz.revery.alarm.model.app.Alarm
import com.pghaz.revery.alarm.viewmodel.ListAlarmsViewModel
import kotlinx.android.synthetic.main.fragment_list_alarms.*


class ListAlarmsFragment : BaseFragment(), OnAlarmClickListener {

    private lateinit var listAlarmsViewModel: ListAlarmsViewModel
    private lateinit var alarmsAdapter: AlarmsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        alarmsAdapter = AlarmsAdapter(this)

        listAlarmsViewModel = ViewModelProvider(this).get(ListAlarmsViewModel::class.java)
        listAlarmsViewModel.alarmsLiveData.observe(this, { alarms ->
            alarmsAdapter.setAlarms(alarms)
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

        context?.let {
            val itemDecor =
                AlarmItemDecoration(ContextCompat.getColor(it, R.color.colorPrimary), 2)
            recyclerView.addItemDecoration(itemDecor)
        }
    }

    private fun configureAddAlarmButton() {
        addAlarmButton.setOnClickListener {
            val createAlarmFragment = CreateEditAlarmFragment.newInstance()
            createAlarmFragment.show(childFragmentManager, CreateEditAlarmFragment.TAG)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_alarms, menu);
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
            if (it.enabled) {
                listAlarmsViewModel.cancelAlarm(context, it)
                listAlarmsViewModel.update(it)
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

    override fun onClick(alarm: Alarm) {
        val createAlarmFragment = CreateEditAlarmFragment.newInstance(
            alarm.id,
            alarm.hour,
            alarm.minute,
            alarm.label,
            alarm.recurring,
            alarm.enabled,
            alarm.monday,
            alarm.tuesday,
            alarm.wednesday,
            alarm.thursday,
            alarm.friday,
            alarm.saturday,
            alarm.sunday,
            alarm.vibrate,
            alarm.metadata
        )
        createAlarmFragment.show(childFragmentManager, CreateEditAlarmFragment.TAG)
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