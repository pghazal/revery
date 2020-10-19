package com.pghaz.revery.alarm.adapter.base

import android.view.View
import com.pghaz.revery.alarm.model.BaseModel

class EmptyViewHolder(itemView: View) : BaseViewHolder(itemView) {
    override fun bind(model: BaseModel) {
        // do nothing
    }

    override fun onViewHolderRecycled() {
        // do nothing
    }
}