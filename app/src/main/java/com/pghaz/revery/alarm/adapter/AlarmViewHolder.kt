package com.pghaz.revery.alarm.adapter

import android.text.TextUtils
import android.view.View
import android.widget.CheckedTextView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.pghaz.revery.R
import com.pghaz.revery.alarm.model.app.Alarm
import com.pghaz.revery.util.DayUtil
import com.squareup.picasso.Picasso
import java.util.*

class AlarmViewHolder(view: View, private val alarmListener: OnAlarmClickListener) :
    RecyclerView.ViewHolder(view) {

    private val timeTextView: TextView = view.findViewById(R.id.timeTextView)
    private val labelTextView: TextView = view.findViewById(R.id.labelTextView)
    private val timeRemainingTextView: TextView = view.findViewById(R.id.timeRemainingTextView)
    private val imageView: ImageView = view.findViewById(R.id.imageView)

    private val recurringDaysContainer: View = view.findViewById(R.id.recurringDaysContainer)
    private val mondayTextView: CheckedTextView = view.findViewById(R.id.mondayTextView)
    private val tuesdayTextView: CheckedTextView = view.findViewById(R.id.tuesdayTextView)
    private val wednesdayTextView: CheckedTextView = view.findViewById(R.id.wednesdayTextView)
    private val thursdayTextView: CheckedTextView = view.findViewById(R.id.thursdayTextView)
    private val fridayTextView: CheckedTextView = view.findViewById(R.id.fridayTextView)
    private val saturdayTextView: CheckedTextView = view.findViewById(R.id.saturdayTextView)
    private val sundayTextView: CheckedTextView = view.findViewById(R.id.sundayTextView)

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

        mondayTextView.isChecked = alarm.monday
        tuesdayTextView.isChecked = alarm.tuesday
        wednesdayTextView.isChecked = alarm.wednesday
        thursdayTextView.isChecked = alarm.thursday
        fridayTextView.isChecked = alarm.friday
        saturdayTextView.isChecked = alarm.saturday
        sundayTextView.isChecked = alarm.sunday

        mondayTextView.isEnabled = alarm.enabled
        tuesdayTextView.isEnabled = alarm.enabled
        wednesdayTextView.isEnabled = alarm.enabled
        thursdayTextView.isEnabled = alarm.enabled
        fridayTextView.isEnabled = alarm.enabled
        saturdayTextView.isEnabled = alarm.enabled
        sundayTextView.isEnabled = alarm.enabled

        alarmSwitch.isChecked = alarm.enabled
        alarmSwitch.setOnCheckedChangeListener { _, _ ->
            alarmListener.onToggle(alarm)
        }

        imageView.isEnabled = alarm.enabled
        timeTextView.isEnabled = alarm.enabled
        labelTextView.isEnabled = alarm.enabled

        Picasso.get().load(alarm.metadata?.imageUrl)
            .placeholder(R.drawable.selector_alarm_image_background_color)
            .into(imageView)

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
