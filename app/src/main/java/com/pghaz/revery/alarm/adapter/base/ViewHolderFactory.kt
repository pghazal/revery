package com.pghaz.revery.alarm.adapter.base

import android.view.View
import com.pghaz.revery.alarm.adapter.AlarmViewHolder

class ViewHolderFactory {

    companion object {
        fun createViewHolder(listItemType: ListItemType, view: View): BaseViewHolder {
            return when (listItemType) {
                ListItemType.Alarm -> AlarmViewHolder(view)
                else -> EmptyViewHolder(view)
            }
        }
    }
}