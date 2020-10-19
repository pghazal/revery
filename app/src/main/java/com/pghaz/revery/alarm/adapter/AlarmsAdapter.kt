package com.pghaz.revery.alarm.adapter

import android.view.ViewGroup
import com.pghaz.revery.alarm.adapter.base.BaseAdapter
import com.pghaz.revery.alarm.adapter.base.BaseViewHolder
import com.pghaz.revery.alarm.adapter.base.ListItemType
import com.pghaz.revery.alarm.model.app.AbstractAlarm

class AlarmsAdapter(
    private val alarmListener: OnAlarmClickListener,
    private val is24HourFormat: Boolean
) : BaseAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val viewHolder = super.onCreateViewHolder(parent, viewType)

        when (ListItemType.values()[viewType]) {
            ListItemType.Alarm -> {
                (viewHolder as AlarmViewHolder).alarmListener = alarmListener
            }
            ListItemType.SpotifyAlarm -> {
                (viewHolder as SpotifyAlarmViewHolder).alarmListener = alarmListener
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (holder is AlarmViewHolder) {
            holder.is24HourFormat = is24HourFormat
        } else if (holder is SpotifyAlarmViewHolder) {
            holder.is24HourFormat = is24HourFormat
        }

        super.onBindViewHolder(holder, position)
    }

    fun setAlarms(newAlarms: List<AbstractAlarm>) {
        this.items = newAlarms
        notifyDataSetChanged()
    }

    override fun onViewRecycled(holder: BaseViewHolder) {
        super.onViewRecycled(holder)
        holder.onViewHolderRecycled()
    }
}