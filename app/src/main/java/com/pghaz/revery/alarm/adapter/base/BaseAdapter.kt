package com.pghaz.revery.alarm.adapter.base

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.pghaz.revery.alarm.model.BaseModel
import com.pghaz.revery.alarm.model.app.Alarm
import java.util.*

abstract class BaseAdapter : ListAdapter<BaseModel, BaseViewHolder>(DiffUtilCallback) {

    var onListChangedListener: OnListChangedListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val itemType = ListItemType.values()[viewType]

        val view = LayoutInflater.from(parent.context)
            .inflate(itemType.layoutResId, parent, false) as View

        return ViewHolderFactory.createViewHolder(itemType, view)
    }

    @CallSuper
    override fun getItemViewType(position: Int): Int {
        return getAddedItemViewType(position)
    }

    abstract fun getAddedItemViewType(position: Int): Int

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    override fun onViewRecycled(holder: BaseViewHolder) {
        super.onViewRecycled(holder)
        holder.onViewHolderRecycled()
    }

    override fun onCurrentListChanged(
        previousList: MutableList<BaseModel>,
        currentList: MutableList<BaseModel>
    ) {
        super.onCurrentListChanged(previousList, currentList)
        onListChangedListener?.onListChanged(previousList, currentList)
    }

    interface OnListChangedListener {
        fun onListChanged(previousList: MutableList<BaseModel>, currentList: MutableList<BaseModel>)
    }

    companion object {
        private val DiffUtilCallback = object : DiffUtil.ItemCallback<BaseModel>() {
            override fun areItemsTheSame(oldItem: BaseModel, newItem: BaseModel): Boolean {
                if (oldItem is Alarm && newItem is Alarm) {
                    return oldItem.id == newItem.id
                }

                return Objects.equals(oldItem, newItem)
            }

            /**
             * Equals method used is the one generated by data classes
             */
            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: BaseModel, newItem: BaseModel): Boolean {
                if (oldItem is Alarm && newItem is Alarm) {
                    return oldItem == newItem
                }

                return Objects.equals(oldItem, newItem)
            }
        }
    }
}