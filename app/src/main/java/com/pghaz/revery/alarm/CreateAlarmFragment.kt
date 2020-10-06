package com.pghaz.revery.alarm

import android.os.Bundle
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.pghaz.revery.BaseDialogFragment
import com.pghaz.revery.R
import com.pghaz.revery.repository.Alarm
import com.pghaz.revery.util.DayUtil
import com.pghaz.revery.viewmodel.CreateAlarmViewModel
import kotlinx.android.synthetic.main.fragment_create_alarm.*

class CreateAlarmFragment : BaseDialogFragment() {

    private lateinit var createAlarmViewModel: CreateAlarmViewModel
    private lateinit var alarm: Alarm
    private val alarmLiveData = MutableLiveData<Alarm>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAlarmViewModel = ViewModelProvider(this).get(CreateAlarmViewModel::class.java)
        alarmLiveData.observe(this, {
            val timeRemainingInfo = DayUtil.getTimeRemaining(it)
            timeRemainingTextView.text =
                DayUtil.getRemainingTimeText(timeRemainingTextView.context, timeRemainingInfo)
        })
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_create_alarm
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        alarm = Alarm(System.currentTimeMillis(), timePicker.hour, timePicker.minute)
        alarmLiveData.value = alarm

        createAlarmButton.setOnClickListener {
            createAndScheduleAlarm()
            dismiss()
        }

        timePicker.setIs24HourView(true)
        timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            alarm.hour = hourOfDay
            alarm.minute = minute
            alarmLiveData.value = alarm
        }

        mondayToggle.setOnCheckedChangeListener { _, _ ->
            alarm.monday = mondayToggle.isChecked
            alarm.recurring = isAlarmRecurring()
            alarmLiveData.value = alarm
        }

        tuesdayToggle.setOnCheckedChangeListener { _, _ ->
            alarm.tuesday = tuesdayToggle.isChecked
            alarm.recurring = isAlarmRecurring()
            alarmLiveData.value = alarm
        }

        wednesdayToggle.setOnCheckedChangeListener { _, _ ->
            alarm.wednesday = wednesdayToggle.isChecked
            alarm.recurring = isAlarmRecurring()
            alarmLiveData.value = alarm
        }

        thursdayToggle.setOnCheckedChangeListener { _, _ ->
            alarm.thursday = thursdayToggle.isChecked
            alarm.recurring = isAlarmRecurring()
            alarmLiveData.value = alarm
        }

        fridayToggle.setOnCheckedChangeListener { _, _ ->
            alarm.friday = fridayToggle.isChecked
            alarm.recurring = isAlarmRecurring()
            alarmLiveData.value = alarm
        }

        saturdayToggle.setOnCheckedChangeListener { _, _ ->
            alarm.saturday = saturdayToggle.isChecked
            alarm.recurring = isAlarmRecurring()
            alarmLiveData.value = alarm
        }

        sundayToggle.setOnCheckedChangeListener { _, _ ->
            alarm.sunday = sundayToggle.isChecked
            alarm.recurring = isAlarmRecurring()
            alarmLiveData.value = alarm
        }
    }

    private fun isAlarmRecurring(): Boolean {
        return mondayToggle.isChecked ||
                tuesdayToggle.isChecked ||
                wednesdayToggle.isChecked ||
                thursdayToggle.isChecked ||
                fridayToggle.isChecked ||
                saturdayToggle.isChecked ||
                sundayToggle.isChecked
    }

    private fun createAndScheduleAlarm() {
        val label: String =
            if (TextUtils.isEmpty(labelEditText.text.trim())) "" else labelEditText.text.trim()
                .toString()

        alarm.label = label
        alarm.enabled = true
        alarm.vibrate = vibrateSwitch.isChecked

        createAlarmViewModel.createAlarm(context, alarm)
    }

    companion object {
        const val TAG = "CreateAlarmFragment"

        fun newInstance(): CreateAlarmFragment {
            return CreateAlarmFragment()
        }
    }
}