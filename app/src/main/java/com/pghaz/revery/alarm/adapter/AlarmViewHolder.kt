package com.pghaz.revery.alarm.adapter

import android.text.TextUtils
import android.view.View
import android.widget.CheckedTextView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.pghaz.revery.R
import com.pghaz.revery.alarm.adapter.base.BaseViewHolder
import com.pghaz.revery.alarm.model.BaseModel
import com.pghaz.revery.alarm.model.app.AbstractAlarm
import com.pghaz.revery.alarm.model.app.Alarm
import com.pghaz.revery.alarm.model.app.SpotifyAlarm
import com.pghaz.revery.extension.logError
import com.pghaz.revery.image.ImageLoader
import com.pghaz.revery.util.DateTimeUtils
import java.util.*

open class AlarmViewHolder(view: View) : BaseViewHolder(view) {
    var is24HourFormat: Boolean = true
    var alarmListener: OnAlarmClickListener? = null

    private val timeTextView: TextView = view.findViewById(R.id.timeTextView)
    private val amPmTextView: TextView = view.findViewById(R.id.amPmTextView)
    private val labelTextView: TextView = view.findViewById(R.id.labelTextView)
    private val timeRemainingTextView: TextView = view.findViewById(R.id.timeRemainingTextView)
    protected val imageView: ImageView = view.findViewById(R.id.imageView)

    private val recurringDaysContainer: View = view.findViewById(R.id.recurringDaysContainer)
    private val recurringLabelTextView: CheckedTextView =
        view.findViewById(R.id.recurringLabelTextView)
    private val mondayTextView: CheckedTextView = view.findViewById(R.id.mondayTextView)
    private val tuesdayTextView: CheckedTextView = view.findViewById(R.id.tuesdayTextView)
    private val wednesdayTextView: CheckedTextView = view.findViewById(R.id.wednesdayTextView)
    private val thursdayTextView: CheckedTextView = view.findViewById(R.id.thursdayTextView)
    private val fridayTextView: CheckedTextView = view.findViewById(R.id.fridayTextView)
    private val saturdayTextView: CheckedTextView = view.findViewById(R.id.saturdayTextView)
    private val sundayTextView: CheckedTextView = view.findViewById(R.id.sundayTextView)

    private val alarmSwitch: SwitchCompat = view.findViewById(R.id.alarmSwitch)

    private fun setTimeText(alarm: AbstractAlarm, is24HourFormat: Boolean) {
        val hour: Int

        if (is24HourFormat) {
            hour = alarm.hour
            amPmTextView.visibility = View.GONE
        } else {
            hour = DateTimeUtils.get12HourFormatFrom24HourFormat(alarm.hour)
            amPmTextView.visibility = View.VISIBLE

            if (DateTimeUtils.isAM(alarm.hour)) {
                amPmTextView.text = amPmTextView.context.getString(R.string.am)
            } else {
                amPmTextView.text = amPmTextView.context.getString(R.string.pm)
            }
        }

        timeTextView.text = String.format(
            Locale.getDefault(), "%02d:%02d",
            hour,
            alarm.minute
        )
    }

    override fun bind(model: BaseModel) {
        val alarm = if (model is Alarm) {
            itemView.context.logError("AlarmViewHolder bind: is Alarm")
            model
        } else {
            itemView.context.logError("AlarmViewHolder bind: is SpotifyAlarm")
            model as SpotifyAlarm
        }

        itemView.setOnClickListener {
            alarmListener?.onClick(alarm)
        }

        setTimeText(alarm, is24HourFormat)

        if (TextUtils.isEmpty(alarm.label)) {
            labelTextView.visibility = View.GONE
        } else {
            labelTextView.visibility = View.VISIBLE
        }

        labelTextView.text = alarm.label

        if (alarm.recurring) {
            val isEveryday = alarm.monday && alarm.tuesday && alarm.wednesday && alarm.thursday &&
                    alarm.friday && alarm.saturday && alarm.sunday
            val isWeekend = alarm.saturday && alarm.sunday && !alarm.monday && !alarm.tuesday &&
                    !alarm.wednesday && !alarm.thursday && !alarm.friday
            val isWeek = alarm.monday && alarm.tuesday && alarm.wednesday && alarm.thursday &&
                    alarm.friday && !alarm.saturday && !alarm.sunday

            if (isEveryday || isWeekend || isWeek) {
                recurringLabelTextView.text = when {
                    isEveryday -> {
                        recurringLabelTextView.context.getString(R.string.everyday)
                    }
                    isWeekend -> {
                        recurringLabelTextView.context.getString(R.string.weekend)
                    }
                    else -> {
                        recurringLabelTextView.context.getString(R.string.week)
                    }
                }
                recurringLabelTextView.visibility = View.VISIBLE
                updateRecurringDaysVisibility(View.GONE)
            } else {
                recurringLabelTextView.visibility = View.GONE
                updateRecurringDaysVisibility(View.VISIBLE)
            }

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

        recurringLabelTextView.isEnabled = alarm.enabled
        mondayTextView.isEnabled = alarm.enabled
        tuesdayTextView.isEnabled = alarm.enabled
        wednesdayTextView.isEnabled = alarm.enabled
        thursdayTextView.isEnabled = alarm.enabled
        fridayTextView.isEnabled = alarm.enabled
        saturdayTextView.isEnabled = alarm.enabled
        sundayTextView.isEnabled = alarm.enabled

        alarmSwitch.isChecked = alarm.enabled
        alarmSwitch.setOnCheckedChangeListener { _, _ ->
            alarmListener?.onToggle(alarm)
        }

        imageView.isEnabled = alarm.enabled
        timeTextView.isEnabled = alarm.enabled
        amPmTextView.isEnabled = alarm.enabled
        labelTextView.isEnabled = alarm.enabled

        ImageLoader.get().load(null)
            .placeholder(R.drawable.selector_alarm_image_background_color)
            .into(imageView)

        if (alarm.enabled) {
            timeRemainingTextView.visibility = View.VISIBLE
            val timeRemainingInfo = DateTimeUtils.getTimeRemaining(alarm)
            timeRemainingTextView.text =
                DateTimeUtils.getRemainingTimeText(timeRemainingTextView.context, timeRemainingInfo)
        } else {
            timeRemainingTextView.visibility = View.GONE
            timeRemainingTextView.text = ""
        }
    }

    private fun updateRecurringDaysVisibility(visibility: Int) {
        mondayTextView.visibility = visibility
        tuesdayTextView.visibility = visibility
        wednesdayTextView.visibility = visibility
        thursdayTextView.visibility = visibility
        fridayTextView.visibility = visibility
        saturdayTextView.visibility = visibility
        sundayTextView.visibility = visibility
    }

    override fun onViewHolderRecycled() {
        itemView.setOnClickListener(null)
        alarmSwitch.setOnCheckedChangeListener(null)
    }
}
