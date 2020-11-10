package com.pghaz.revery.adapter.timer

import android.view.ViewGroup
import com.pghaz.revery.adapter.base.BaseAdapter
import com.pghaz.revery.adapter.base.BaseViewHolder
import com.pghaz.revery.adapter.base.ListItemType
import com.pghaz.revery.model.app.Timer

class TimersAdapter(private val timerClickListener: OnTimerClickListener) : BaseAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val viewHolder = super.onCreateViewHolder(parent, viewType)

        when (ListItemType.values()[viewType]) {
            ListItemType.Timer -> {
                (viewHolder as TimerViewHolder).timerClickListener = timerClickListener
            }
            else -> {
                // do nothing for now
            }
        }

        return viewHolder
    }

    override fun getAddedItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is Timer -> ListItemType.Timer
            else -> ListItemType.Empty
        }.ordinal
    }
}