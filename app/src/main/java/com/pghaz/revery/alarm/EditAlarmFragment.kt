package com.pghaz.revery.alarm

import android.os.Bundle
import android.text.TextUtils
import androidx.lifecycle.ViewModelProvider
import com.pghaz.revery.BaseDialogFragment
import com.pghaz.revery.R
import com.pghaz.revery.repository.Alarm
import com.pghaz.revery.viewmodel.EditAlarmViewModel
import kotlinx.android.synthetic.main.fragment_edit_alarm.*

class EditAlarmFragment : BaseDialogFragment() {

    private lateinit var editAlarmViewModel: EditAlarmViewModel
    private lateinit var alarm: Alarm

    override fun getLayoutResId(): Int {
        return R.layout.fragment_edit_alarm
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        editAlarmViewModel = ViewModelProvider(this).get(EditAlarmViewModel::class.java)

        arguments?.let { args ->
            alarm = Alarm(
                args.getLong(Alarm.ID, NO_ID),
                args.getInt(Alarm.HOUR, 0),
                args.getInt(Alarm.MINUTE, 0),
                args.getString(Alarm.LABEL),
                args.getBoolean(Alarm.RECURRING),
                args.getBoolean(Alarm.ENABLED),
                args.getBoolean(Alarm.MONDAY),
                args.getBoolean(Alarm.TUESDAY),
                args.getBoolean(Alarm.WEDNESDAY),
                args.getBoolean(Alarm.THURSDAY),
                args.getBoolean(Alarm.FRIDAY),
                args.getBoolean(Alarm.SATURDAY),
                args.getBoolean(Alarm.SUNDAY),
                args.getBoolean(Alarm.VIBRATE)
            )
        }
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        timePicker.setIs24HourView(true)
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

        updateAlarmButton.setOnClickListener {
            val recurring = mondayToggle.isChecked ||
                    tuesdayToggle.isChecked ||
                    wednesdayToggle.isChecked ||
                    thursdayToggle.isChecked ||
                    fridayToggle.isChecked ||
                    saturdayToggle.isChecked ||
                    sundayToggle.isChecked

            val label: String =
                if (TextUtils.isEmpty(labelEditText.text.trim())) "" else labelEditText.text.trim()
                    .toString()

            val updatedAlarm = Alarm(
                alarm.id,
                timePicker.hour,
                timePicker.minute,
                label,
                recurring,
                true,
                mondayToggle.isChecked,
                tuesdayToggle.isChecked,
                wednesdayToggle.isChecked,
                thursdayToggle.isChecked,
                fridayToggle.isChecked,
                saturdayToggle.isChecked,
                sundayToggle.isChecked,
                vibrateSwitch.isChecked
            )

            editAlarmViewModel.edit(context, updatedAlarm)

            dismiss()
        }

        deleteAlarmButton.setOnClickListener {
            editAlarmViewModel.delete(context, alarm)
            dismiss()
        }
    }

    companion object {
        const val TAG = "EditAlarmFragment"
        private const val NO_ID: Long = 0

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
        ): EditAlarmFragment {
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

            val fragment = EditAlarmFragment()
            fragment.arguments = args

            return fragment
        }
    }
}