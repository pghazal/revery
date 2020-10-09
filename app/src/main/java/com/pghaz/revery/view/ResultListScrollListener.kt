package com.pghaz.revery.view

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ResultListScrollListener(
    private val mLayoutManager: LinearLayoutManager,
    private val onLoadMoreListener: OnLoadMoreListener?
) :
    RecyclerView.OnScrollListener() {

    private var mCurrentItemCount = 0
    private var mAwaitingItems = true

    fun reset() {
        mCurrentItemCount = 0
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        val itemCount = mLayoutManager.itemCount
        val itemPosition = mLayoutManager.findLastVisibleItemPosition()
        if (mAwaitingItems && itemCount > mCurrentItemCount) {
            mCurrentItemCount = itemCount
            mAwaitingItems = false
        }

        if (!mAwaitingItems && itemPosition + 1 >= itemCount - SCROLL_BUFFER) {
            mAwaitingItems = true
            onLoadMoreListener?.onLoadMore()
        }
    }

    interface OnLoadMoreListener {
        fun onLoadMore()
    }

    companion object {
        private const val SCROLL_BUFFER = 5
    }
}