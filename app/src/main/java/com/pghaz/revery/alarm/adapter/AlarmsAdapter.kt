package com.pghaz.revery.alarm.adapter

import android.view.ViewGroup
import com.pghaz.revery.alarm.adapter.base.BaseAdapter
import com.pghaz.revery.alarm.adapter.base.BaseViewHolder
import com.pghaz.revery.alarm.adapter.base.ListItemType

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
            else -> {
                // do nothing for now
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (holder is AlarmViewHolder) {
            holder.is24HourFormat = is24HourFormat
        }

        super.onBindViewHolder(holder, position)
    }

    override fun onViewRecycled(holder: BaseViewHolder) {
        super.onViewRecycled(holder)
        holder.onViewHolderRecycled()
    }
}