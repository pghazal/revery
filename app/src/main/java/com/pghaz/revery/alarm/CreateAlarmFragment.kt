package com.pghaz.revery.alarm

import android.os.Bundle
import android.text.TextUtils
import androidx.lifecycle.ViewModelProvider
import com.pghaz.revery.BaseDialogFragment
import com.pghaz.revery.R
import com.pghaz.revery.repository.Alarm
import com.pghaz.revery.viewmodel.CreateAlarmViewModel
import kotlinx.android.synthetic.main.fragment_create_alarm.*

class CreateAlarmFragment : BaseDialogFragment() {

    private lateinit var createAlarmViewModel: CreateAlarmViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAlarmViewModel = ViewModelProvider(this).get(CreateAlarmViewModel::class.java)
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_create_alarm
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        timePicker.setIs24HourView(true)

        createAlarmButton.setOnClickListener {
            createAndScheduleAlarm()
            dismiss()
        }
    }

    private fun createAndScheduleAlarm() {
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

        val alarm = Alarm(
            System.currentTimeMillis(),
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

        createAlarmViewModel.createAlarm(context, alarm)
    }

    companion object {
        const val TAG = "CreateAlarmFragment"

        fun newInstance(): CreateAlarmFragment {
            return CreateAlarmFragment()
        }
    }
}