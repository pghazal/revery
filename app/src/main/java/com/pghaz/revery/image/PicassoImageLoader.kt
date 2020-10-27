package com.pghaz.revery.image

import android.widget.ImageView
import com.pghaz.revery.R
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation

class PicassoImageLoader : IImageLoader {

    private val picasso = Picasso.get()

    private var url: String? = null
    private var placeholderResId: Int? = null
    private var defaultPlaceholderResId: Int = R.drawable.placeholder_square
    private var transformation: Transformation? = null

    override fun load(url: String?): IImageLoader {
        this.url = url
        return this
    }

    override fun placeholder(placeholderResId: Int): IImageLoader {
        this.placeholderResId = placeholderResId
        return this
    }

    override fun transform(transformation: Transformation): IImageLoader {
        this.transformation = transformation
        return this
    }

    override fun into(imageView: ImageView?) {
        val requestCreator = picasso.load(url)

        placeholderResId?.let {
            requestCreator.placeholder(it)
        } ?: run {
            requestCreator.placeholder(defaultPlaceholderResId)
        }

        transformation?.let {
            requestCreator.transform(it)
        }
        requestCreator.into(imageView)
    }
}