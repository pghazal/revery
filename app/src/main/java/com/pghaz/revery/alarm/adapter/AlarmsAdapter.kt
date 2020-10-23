package com.pghaz.revery.alarm.adapter

import android.view.ViewGroup
import com.pghaz.revery.alarm.adapter.base.BaseAdapter
import com.pghaz.revery.alarm.adapter.base.BaseViewHolder
import com.pghaz.revery.alarm.adapter.base.ListItemType
import com.pghaz.revery.alarm.model.app.Alarm

class AlarmsAdapter(
    private val alarmClickListener: OnAlarmClickListener,
    private val is24HourFormat: Boolean
) : BaseAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val viewHolder = super.onCreateViewHolder(parent, viewType)

        when (ListItemType.values()[viewType]) {
            ListItemType.Alarm -> {
                (viewHolder as AlarmViewHolder).alarmClickListener = alarmClickListener
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

    override fun getAddedItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is Alarm -> ListItemType.Alarm
            else -> ListItemType.Empty
        }.ordinal
    }
}