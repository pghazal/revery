package com.pghaz.revery.alarm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pghaz.revery.R
import com.pghaz.revery.alarm.repository.Alarm


class AlarmsAdapter(private val alarmListener: OnAlarmClickListener) :
    RecyclerView.Adapter<AlarmViewHolder>() {

    private var alarms: List<Alarm> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_view_alarm, parent, false) as View

        return AlarmViewHolder(view, alarmListener)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarms[position]
        holder.bind(alarm)
    }

    override fun getItemCount(): Int {
        return alarms.count()
    }

    fun setAlarms(newAlarms: List<Alarm>) {
        this.alarms = newAlarms
        notifyDataSetChanged()
    }

    override fun onViewRecycled(holder: AlarmViewHolder) {
        super.onViewRecycled(holder)
        holder.itemView.setOnClickListener(null)
        holder.alarmSwitch.setOnCheckedChangeListener(null)
    }
}