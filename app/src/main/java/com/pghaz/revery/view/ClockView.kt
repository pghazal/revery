package com.pghaz.revery.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.pghaz.revery.R
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class ClockView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private var mAttached = false
    private val mHandler = Handler()

    private var timeFormatter = SimpleDateFormat.getTimeInstance(DateFormat.SHORT)
    private var dateFormatter = SimpleDateFormat.getDateInstance(DateFormat.FULL)
    private var mCalendar: Calendar = Calendar.getInstance()

    private val timeTextView: TextView
    private val dateTextView: TextView

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_clock, this)
        timeTextView = view.findViewById(R.id.timeTextView)
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
        timeTextView.text = timeFormatter.format(mCalendar.time)
        dateTextView.text = dateFormatter.format(mCalendar.time)
    }

    private fun onTimeChanged() {
        mCalendar.time = Date()
        updateContentDescription(mCalendar)
    }

    private fun updateContentDescription(calendar: Calendar?) {
        contentDescription = calendar.toString()
    }
}