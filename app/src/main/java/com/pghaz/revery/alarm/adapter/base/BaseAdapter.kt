package com.pghaz.revery.alarm.adapter.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pghaz.revery.alarm.model.BaseModel
import com.pghaz.revery.alarm.model.app.Alarm

abstract class BaseAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    protected var items: List<BaseModel> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val itemType = ListItemType.values()[viewType]

        val view = LayoutInflater.from(parent.context)
            .inflate(itemType.layoutResId, parent, false) as View

        return ViewHolderFactory.createViewHolder(itemType, view)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is Alarm -> ListItemType.Alarm
            else -> ListItemType.Empty
        }.ordinal
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return items.count()
    }
}