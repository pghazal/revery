package com.pghaz.revery.image

import android.widget.ImageView
import com.pghaz.revery.R
import com.squareup.picasso.Picasso

class PicassoImageLoader : IImageLoader {

    private val picasso = Picasso.get()

    private var url: String? = null
    private var placeholderResId: Int = R.drawable.placeholder // default

    override fun load(url: String?): IImageLoader {
        this.url = url
        return this
    }

    override fun placeholder(placeholderResId: Int): IImageLoader {
        this.placeholderResId = placeholderResId
        return this
    }

    override fun into(imageView: ImageView?) {
        picasso.load(url).placeholder(placeholderResId).into(imageView)
    }
}