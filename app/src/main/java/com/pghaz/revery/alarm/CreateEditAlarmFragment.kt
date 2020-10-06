package com.pghaz.revery.alarm

import android.os.Bundle
import android.text.TextUtils
import androidx.lifecycle.ViewModelProvider
import com.pghaz.revery.BaseDialogFragment
import com.pghaz.revery.R
import com.pghaz.revery.repository.Alarm
import com.pghaz.revery.util.DayUtil
import com.pghaz.revery.viewmodel.CreateEditAlarmViewModel
import kotlinx.android.synthetic.main.fragment_create_edit_alarm.*

class CreateEditAlarmFragment : BaseDialogFragment() {

    private lateinit var createEditAlarmViewModel: CreateEditAlarmViewModel
    private lateinit var alarm: Alarm

    private fun initAlarmFromArguments(arguments: Bundle?) {
        arguments?.let { args ->
            alarm = Alarm(
                args.getLong(Alarm.ID, Alarm.NO_ID),
                args.getInt(Alarm.HOUR, 0),
                args.getInt(Alarm.MINUTE, 0),
                args.getString(Alarm.LABEL, ""),
                args.getBoolean(Alarm.RECURRING, false),
                args.getBoolean(Alarm.ENABLED, true),
                args.getBoolean(Alarm.MONDAY, false),
                args.getBoolean(Alarm.TUESDAY, false),
                args.getBoolean(Alarm.WEDNESDAY, false),
                args.getBoolean(Alarm.THURSDAY, false),
                args.getBoolean(Alarm.FRIDAY, false),
                args.getBoolean(Alarm.SATURDAY, false),
                args.getBoolean(Alarm.SUNDAY, false),
                args.getBoolean(Alarm.VIBRATE, false)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initAlarmFromArguments(arguments)

        createEditAlarmViewModel = ViewModelProvider(this).get(CreateEditAlarmViewModel::class.java)
        createEditAlarmViewModel.alarmLiveData.observe(this, {
            val timeRemainingInfo = DayUtil.getTimeRemaining(it)
            timeRemainingTextView.text =
                DayUtil.getRemainingTimeText(timeRemainingTextView.context, timeRemainingInfo)
        })
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_create_edit_alarm
    }

    private fun updateViewsFromAlarm(alarm: Alarm) {
        timePicker.hour = alarm.hour
        timePicker.minute = alarm.minute

        labelEditText.setText(alarm.label)

        mondayToggle.isChecked = alarm.monday
        tuesdayToggle.isChecked = alarm.tuesday
        wednesdayToggle.isChecked = alarm.wednesday
        thursdayToggle.isChecked = alarm.thursday
        fridayToggle.isChecked = alarm.friday
        saturdayToggle.isChecked = alarm.saturday
        sundayToggle.isChecked = alarm.sunday

        vibrateSwitch.isChecked = alarm.vibrate
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        timePicker.setIs24HourView(true)

        // If we're editing an alarm, we set time/minute on the time picker
        if (alarm.id != Alarm.NO_ID) {
            // Update views before any listeners are set
            updateViewsFromAlarm(alarm)
            // Also edit negative button
            negativeAlarmButton.text = getString(R.string.delete)
        } else {
            alarm.hour = timePicker.hour
            alarm.minute = timePicker.minute
            negativeAlarmButton.text = getString(R.string.cancel)
        }

        // Notify the LiveData so that it updates time remaining
        createEditAlarmViewModel.alarmLiveData.value = alarm

        positiveAlarmButton.setOnClickListener {
            // This is a creation
            if (Alarm.NO_ID == alarm.id) {
                createAndScheduleAlarm()
            } else { // This is an update
                editAndScheduleAlarm()
            }

            dismiss()
        }

        negativeAlarmButton.setOnClickListener {
            // If we were creating an alarm and click the negative button, just dismiss the dialog
            if (Alarm.NO_ID == alarm.id) {
                // do nothing
            } else { // otherwise delete the existing alarm
                createEditAlarmViewModel.delete(context, alarm)
            }

            dismiss()
        }

        timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            alarm.hour = hourOfDay
            alarm.minute = minute
            createEditAlarmViewModel.alarmLiveData.value = alarm
        }

        mondayToggle.setOnCheckedChangeListener { _, _ ->
            alarm.monday = mondayToggle.isChecked
            alarm.recurring = isAlarmRecurring()
            createEditAlarmViewModel.alarmLiveData.value = alarm
        }

        tuesdayToggle.setOnCheckedChangeListener { _, _ ->
            alarm.tuesday = tuesdayToggle.isChecked
            alarm.recurring = isAlarmRecurring()
            createEditAlarmViewModel.alarmLiveData.value = alarm
        }

        wednesdayToggle.setOnCheckedChangeListener { _, _ ->
            alarm.wednesday = wednesdayToggle.isChecked
            alarm.recurring = isAlarmRecurring()
            createEditAlarmViewModel.alarmLiveData.value = alarm
        }

        thursdayToggle.setOnCheckedChangeListener { _, _ ->
            alarm.thursday = thursdayToggle.isChecked
            alarm.recurring = isAlarmRecurring()
            createEditAlarmViewModel.alarmLiveData.value = alarm
        }

        fridayToggle.setOnCheckedChangeListener { _, _ ->
            alarm.friday = fridayToggle.isChecked
            alarm.recurring = isAlarmRecurring()
            createEditAlarmViewModel.alarmLiveData.value = alarm
        }

        saturdayToggle.setOnCheckedChangeListener { _, _ ->
            alarm.saturday = saturdayToggle.isChecked
            alarm.recurring = isAlarmRecurring()
            createEditAlarmViewModel.alarmLiveData.value = alarm
        }

        sundayToggle.setOnCheckedChangeListener { _, _ ->
            alarm.sunday = sundayToggle.isChecked
            alarm.recurring = isAlarmRecurring()
            createEditAlarmViewModel.alarmLiveData.value = alarm
        }

        vibrateSwitch.setOnCheckedChangeListener { _, _ ->
            alarm.vibrate = vibrateSwitch.isChecked
            createEditAlarmViewModel.alarmLiveData.value = alarm
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

        alarm.id = System.currentTimeMillis()
        alarm.label = label
        alarm.enabled = true

        createEditAlarmViewModel.createAlarm(context, alarm)
    }

    private fun editAndScheduleAlarm() {
        val label: String =
            if (TextUtils.isEmpty(labelEditText.text.trim())) "" else labelEditText.text.trim()
                .toString()

        alarm.label = label
        alarm.enabled = true

        createEditAlarmViewModel.editAlarm(context, alarm)
    }

    companion object {
        const val TAG = "CreateAlarmFragment"

        fun newInstance(): CreateEditAlarmFragment {
            return newInstance(
                Alarm.NO_ID,
                0, 0,
                "",
                recurring = false,
                enabled = true,
                monday = false,
                tuesday = false,
                wednesday = false,
                thursday = false,
                friday = false,
                saturday = false,
                sunday = false,
                vibrate = false
            )
        }

        fun newInstance(
            id: Long,
            hour: Int,
            minute: Int,
            label: String?,
            recurring: Boolean,
            enabled: Boolean,
            monday: Boolean,
            tuesday: Boolean,
            wednesday: Boolean,
            thursday: Boolean,
            friday: Boolean,
            saturday: Boolean,
            sunday: Boolean,
            vibrate: Boolean,
        ): CreateEditAlarmFragment {
            val args = Bundle()

            args.putLong(Alarm.ID, id)
            args.putInt(Alarm.HOUR, hour)
            args.putInt(Alarm.MINUTE, minute)
            args.putString(Alarm.LABEL, label)
            args.putBoolean(Alarm.RECURRING, recurring)
            args.putBoolean(Alarm.ENABLED, enabled)
            args.putBoolean(Alarm.MONDAY, monday)
            args.putBoolean(Alarm.TUESDAY, tuesday)
            args.putBoolean(Alarm.WEDNESDAY, wednesday)
            args.putBoolean(Alarm.THURSDAY, thursday)
            args.putBoolean(Alarm.FRIDAY, friday)
            args.putBoolean(Alarm.SATURDAY, saturday)
            args.putBoolean(Alarm.SUNDAY, sunday)
            args.putBoolean(Alarm.VIBRATE, vibrate)

            val fragment = CreateEditAlarmFragment()
            fragment.arguments = args

            return fragment
        }
    }
}