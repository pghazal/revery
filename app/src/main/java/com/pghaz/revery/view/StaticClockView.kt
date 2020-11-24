package com.pghaz.revery.view

import android.content.Context
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.pghaz.revery.R
import com.pghaz.revery.util.DateTimeUtils
import java.text.SimpleDateFormat
import java.util.*

class StaticClockView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var mCalendar: Calendar = Calendar.getInstance()
    private val time24HourFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val time12HourFormatter = SimpleDateFormat("hh:mm", Locale.getDefault())
    private val timeAmPmFormatter = SimpleDateFormat("a", Locale.getDefault())
    private val is24HourFormat = android.text.format.DateFormat.is24HourFormat(context)

    private val timeTextView: TextView
    private val amPmTextView: TextView
    private val dateTextView: TextView

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_clock, this)
        timeTextView = view.findViewById(R.id.timeTextView)
        amPmTextView = view.findViewById(R.id.amPmTextView)
        dateTextView = view.findViewById(R.id.dateTextView)
    }

    private fun updateView() {
        val date = mCalendar.time

        if (is24HourFormat) {
            amPmTextView.visibility = View.GONE
            timeTextView.text = time24HourFormatter.format(date)
        } else {
            amPmTextView.visibility = View.VISIBLE
            amPmTextView.text = timeAmPmFormatter.format(date)
            timeTextView.text = time12HourFormatter.format(date)
        }

        dateTextView.text = DateUtils.formatDateTime(
            context,
            date.time,
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY
        )
    }

    private fun updateContentDescription(calendar: Calendar?) {
        contentDescription = calendar.toString()
    }

    fun updateTime(hour: Int, minute: Int) {
        mCalendar.time = Date()

        mCalendar.set(Calendar.HOUR_OF_DAY, hour)
        mCalendar.set(Calendar.MINUTE, minute)
        mCalendar.set(Calendar.SECOND, 0)
        mCalendar.set(Calendar.MILLISECOND, 0)

        // if alarm time has already passed, increment day by 1
        if (mCalendar.timeInMillis <= System.currentTimeMillis()) {
            DateTimeUtils.incrementByOneDay(mCalendar)
        }

        updateContentDescription(mCalendar)
        updateView()
    }
}