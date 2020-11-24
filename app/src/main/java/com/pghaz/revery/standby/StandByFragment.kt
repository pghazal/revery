package com.pghaz.revery.standby

import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.format.DateFormat
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import android.widget.TimePicker
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.pghaz.revery.BaseFragment
import com.pghaz.revery.R
import com.pghaz.revery.broadcastreceiver.StandByBroadcastReceiver
import com.pghaz.revery.model.app.StandByEnabler
import com.pghaz.revery.settings.FadeDuration
import com.pghaz.revery.settings.SettingsFragment
import com.pghaz.revery.util.IntentUtils
import com.pghaz.revery.viewmodel.standby.StandByViewModel
import kotlinx.android.synthetic.main.fragment_standby.*
import java.util.*


class StandByFragment : BaseFragment(), TimePickerDialog.OnTimeSetListener {

    inner class LocalStandByBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (context == null || intent == null) {
                return
            }

            if (ACTION_STANDBY_STARTED == intent.action) {
                val standByEnabler = IntentUtils.safeGetStandByEnablerFromIntent(intent)
                updateStandbyView(standByEnabler)
            }
        }
    }

    private var receiver = LocalStandByBroadcastReceiver()

    private lateinit var standbyViewModel: StandByViewModel

    private var is24HourFormat: Boolean = false
    private lateinit var timePickerDialog: TimePickerDialog

    override fun getLayoutResId(): Int {
        return R.layout.fragment_standby
    }

    private fun registerToLocalBroadcastReceiver() {
        context?.let {
            val intentFilter = IntentFilter()
            intentFilter.addAction(StandByBroadcastReceiver.ACTION_STANDBY_START)
            LocalBroadcastManager.getInstance(it).registerReceiver(receiver, intentFilter)
        }
    }

    private fun unregisterFromLocalBroadcastReceiver() {
        context?.let { LocalBroadcastManager.getInstance(it).unregisterReceiver(receiver) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        timePickerDialog.onSaveInstanceState()
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        registerToLocalBroadcastReceiver()

        standbyViewModel = ViewModelProvider(this).get(StandByViewModel::class.java)
        standbyViewModel.standbyLiveData.observe(this, {
            updateStandbyView(it)

            if (it.enabled) {
                StandByHandler.removeAlarm(context, it)
                StandByHandler.setAlarm(context, is24HourFormat, it)
            } else {
                StandByHandler.removeAlarm(context, it)
            }
        })

        standbyViewModel.standbyLiveData.value = standbyViewModel.getStandByEnabler(context)
    }

    private fun updateStandbyView(standByEnabler: StandByEnabler) {
        if (standByEnabler.enabled) {
            standbyEnabledTextView.visibility = View.VISIBLE
            clockView.visibility = View.VISIBLE
            standbyDisabledTextView.visibility = View.GONE
            fadeOutToggle.visibility = View.VISIBLE
            if (standByEnabler.fadeOut) {
                fadeOutDurationSpinner.visibility = View.VISIBLE
            } else {
                fadeOutDurationSpinner.visibility = View.GONE
            }
        } else {
            standbyEnabledTextView.visibility = View.GONE
            clockView.visibility = View.GONE
            standbyDisabledTextView.visibility = View.VISIBLE
            fadeOutToggle.visibility = View.GONE
            fadeOutDurationSpinner.visibility = View.GONE
        }

        clockView.updateTime(standByEnabler.hour, standByEnabler.minute)
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        is24HourFormat = DateFormat.is24HourFormat(context)

        timePickerDialog = TimePickerDialog(context, this, 0, 0, is24HourFormat)
        timePickerDialog.setOnCancelListener {
            if (!standbyViewModel.getStandByEnabler(timePickerDialog.context).enabled) {
                standbySwitch.isChecked = false
            }
        }

        savedInstanceState?.let { timePickerDialog.onRestoreInstanceState(it) }

        standbySwitch.isChecked = standbyViewModel.getStandByEnabler(context).enabled
        standbySwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                val calendar = Calendar.getInstance()
                val hour = calendar[if (is24HourFormat) Calendar.HOUR_OF_DAY else Calendar.HOUR]
                val minute = calendar[Calendar.MINUTE]
                showTimerPickerDialog(hour, minute)
            } else {
                standbyViewModel.setStandByEnabled(buttonView.context, false)
                standbyViewModel.standbyLiveData.value =
                    standbyViewModel.getStandByEnabler(buttonView.context)
            }
        }

        fadeOutToggle.isChecked = standbyViewModel.getStandByEnabler(context).fadeOut
        fadeOutToggle.setOnCheckedChangeListener { buttonView, isChecked ->
            standbyViewModel.setStandByFadeOut(buttonView.context, isChecked)
            standbyViewModel.standbyLiveData.value =
                standbyViewModel.getStandByEnabler(buttonView.context)
        }

        clockView.setOnClickListener {
            val standByEnabler = standbyViewModel.getStandByEnabler(it.context)
            showTimerPickerDialog(standByEnabler.hour, standByEnabler.minute)
        }

        val spinnerAdapter: SpinnerAdapter = ArrayAdapter(
            fadeOutDurationSpinner.context,
            R.layout.spinner_item_view,
            resources.getStringArray(R.array.fade_in_duration_array)
        )
        fadeOutDurationSpinner.adapter = spinnerAdapter

        val fadeOutDurationPosition =
            standbyViewModel.getStandByFadeOutDurationPosition(fadeOutDurationSpinner.context)
        fadeOutDurationSpinner.setSelection(fadeOutDurationPosition)

        fadeOutDurationSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?,
                position: Int, id: Long
            ) {
                if (context == null) {
                    return
                }

                when (position) {
                    FadeDuration.TEN_SECONDS.ordinal -> {
                        standbyViewModel.setStandByFadeOutDuration(
                            context!!,
                            FadeDuration.TEN_SECONDS
                        )
                    }
                    FadeDuration.TWENTY_SECONDS.ordinal -> {
                        standbyViewModel.setStandByFadeOutDuration(
                            context!!,
                            FadeDuration.TWENTY_SECONDS
                        )
                    }
                    FadeDuration.THIRTY_SECONDS.ordinal -> {
                        standbyViewModel.setStandByFadeOutDuration(
                            context!!,
                            FadeDuration.THIRTY_SECONDS
                        )
                    }
                    FadeDuration.ONE_MINUTE.ordinal -> {
                        standbyViewModel.setStandByFadeOutDuration(
                            context!!,
                            FadeDuration.ONE_MINUTE
                        )
                    }
                    FadeDuration.TWO_MINUTES.ordinal -> {
                        standbyViewModel.setStandByFadeOutDuration(
                            context!!,
                            FadeDuration.TWO_MINUTES
                        )
                    }
                    FadeDuration.FIVE_MINUTES.ordinal -> {
                        standbyViewModel.setStandByFadeOutDuration(
                            context!!,
                            FadeDuration.FIVE_MINUTES
                        )
                    }
                }

                standbyViewModel.standbyLiveData.value =
                    standbyViewModel.getStandByEnabler(context!!)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // do nothing
            }
        }
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        view?.context?.let {
            standbyViewModel.setStandByEnabled(it, true)
            standbyViewModel.setStandByHour(it, hourOfDay)
            standbyViewModel.setStandByMinute(it, minute)

            standbyViewModel.standbyLiveData.value = standbyViewModel.getStandByEnabler(it)
        }
    }

    private fun showTimerPickerDialog(hour: Int, minute: Int) {
        timePickerDialog.updateTime(hour, minute)
        timePickerDialog.show()
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

    override fun onDestroy() {
        unregisterFromLocalBroadcastReceiver()
        super.onDestroy()
    }

    companion object {
        const val TAG = "StandByFragment"

        const val ACTION_STANDBY_STARTED = "com.pghaz.revery.ACTION_STANDBY_START"

        fun buildStandByStartedIntent(context: Context, standByEnabler: StandByEnabler): Intent {
            val intent =
                Intent(context.applicationContext, LocalStandByBroadcastReceiver::class.java)
            intent.action = ACTION_STANDBY_STARTED

            IntentUtils.safePutStandByEnablerIntoIntent(intent, standByEnabler)

            return intent
        }

        fun newInstance(): StandByFragment {
            val fragment = StandByFragment()

            val args = Bundle()
            fragment.arguments = args

            return fragment
        }
    }
}