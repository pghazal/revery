package com.pghaz.revery.adapter.base

import android.view.View
import com.pghaz.revery.model.app.BaseModel

class EmptyViewHolder(itemView: View) : BaseViewHolder(itemView) {
    override fun bind(model: BaseModel) {
        // do nothing
    }

    override fun onViewHolderRecycled() {
        // do nothing
    }
}