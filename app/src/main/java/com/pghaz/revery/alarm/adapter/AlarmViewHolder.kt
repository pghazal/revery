package com.pghaz.revery.alarm.adapter

import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.pghaz.revery.R
import com.pghaz.revery.alarm.repository.Alarm
import com.pghaz.revery.util.DayUtil
import java.util.*

class AlarmViewHolder(view: View, private val alarmListener: OnAlarmClickListener) :
    RecyclerView.ViewHolder(view) {

    private val timeTextView: TextView = view.findViewById(R.id.timeTextView)
    private val labelTextView: TextView = view.findViewById(R.id.labelTextView)
    private val timeRemainingTextView: TextView = view.findViewById(R.id.timeRemainingTextView)

    private val recurringDaysContainer: View = view.findViewById(R.id.recurringDaysContainer)
    private val mondayTextView: TextView = view.findViewById(R.id.mondayTextView)
    private val tuesdayTextView: TextView = view.findViewById(R.id.tuesdayTextView)
    private val wednesdayTextView: TextView = view.findViewById(R.id.wednesdayTextView)
    private val thursdayTextView: TextView = view.findViewById(R.id.thursdayTextView)
    private val fridayTextView: TextView = view.findViewById(R.id.fridayTextView)
    private val saturdayTextView: TextView = view.findViewById(R.id.saturdayTextView)
    private val sundayTextView: TextView = view.findViewById(R.id.sundayTextView)

    val alarmSwitch: SwitchCompat = view.findViewById(R.id.alarmSwitch)

    fun bind(alarm: Alarm) {
        itemView.setOnClickListener {
            alarmListener.onClick(alarm)
        }

        timeTextView.text = String.format(
            Locale.getDefault(), "%02d:%02d",
            alarm.hour,
            alarm.minute
        )

        if (TextUtils.isEmpty(alarm.label)) {
            labelTextView.visibility = View.GONE
        } else {
            labelTextView.visibility = View.VISIBLE
        }

        labelTextView.text = alarm.label

        if (alarm.recurring) {
            recurringDaysContainer.visibility = View.VISIBLE
        } else {
            recurringDaysContainer.visibility = View.GONE
        }

        mondayTextView.isEnabled = alarm.monday
        tuesdayTextView.isEnabled = alarm.tuesday
        wednesdayTextView.isEnabled = alarm.wednesday
        thursdayTextView.isEnabled = alarm.thursday
        fridayTextView.isEnabled = alarm.friday
        saturdayTextView.isEnabled = alarm.saturday
        sundayTextView.isEnabled = alarm.sunday

        alarmSwitch.isChecked = alarm.enabled
        alarmSwitch.setOnCheckedChangeListener { _, _ ->
            alarmListener.onToggle(alarm)
        }

        if (alarm.enabled) {
            timeRemainingTextView.visibility = View.VISIBLE
            val timeRemainingInfo = DayUtil.getTimeRemaining(alarm)
            timeRemainingTextView.text =
                DayUtil.getRemainingTimeText(timeRemainingTextView.context, timeRemainingInfo)
        } else {
            timeRemainingTextView.visibility = View.GONE
            timeRemainingTextView.text = ""
        }
    }

}
