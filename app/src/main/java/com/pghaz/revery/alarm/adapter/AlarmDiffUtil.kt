package com.pghaz.revery.alarm.adapter

import androidx.recyclerview.widget.DiffUtil
import com.pghaz.revery.alarm.model.app.Alarm

class AlarmDiffUtil(
    private val oldList: List<Alarm>,
    private val newList: List<Alarm>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}