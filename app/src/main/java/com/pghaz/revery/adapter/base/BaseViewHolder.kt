package com.pghaz.revery.adapter.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.pghaz.revery.model.app.BaseModel

abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract fun bind(model: BaseModel)

    abstract fun onViewHolderRecycled()
}