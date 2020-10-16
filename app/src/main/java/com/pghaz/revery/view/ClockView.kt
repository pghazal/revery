package com.pghaz.revery.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.pghaz.revery.R
import java.text.SimpleDateFormat
import java.util.*

class ClockView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private var mAttached = false
    private val mHandler = Handler()

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

    private val mIntentReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Intent.ACTION_TIMEZONE_CHANGED == intent.action) {
                val timezone = intent.getStringExtra("time-zone")
                mCalendar.timeZone = TimeZone.getTimeZone(timezone)
            }

            onTimeChanged()
            updateView()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!mAttached) {
            mAttached = true
            val filter = IntentFilter()
            filter.addAction(Intent.ACTION_TIME_TICK)
            filter.addAction(Intent.ACTION_TIME_CHANGED)
            filter.addAction(Intent.ACTION_DATE_CHANGED)
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
            context.registerReceiver(mIntentReceiver, filter, null, mHandler)
        }

        // NOTE: It's safe to do these after registering the receiver since the receiver always runs
        // in the main thread, therefore the receiver can't run before this method returns.

        // The time zone may have changed while the receiver wasn't registered, so update the Time
        mCalendar = Calendar.getInstance()

        // Make sure we update to the current time
        onTimeChanged()
        updateView()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (mAttached) {
            context.unregisterReceiver(mIntentReceiver)
            mAttached = false
        }
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

    private fun onTimeChanged() {
        mCalendar.time = Date()
        updateContentDescription(mCalendar)
    }

    private fun updateContentDescription(calendar: Calendar?) {
        contentDescription = calendar.toString()
    }
}