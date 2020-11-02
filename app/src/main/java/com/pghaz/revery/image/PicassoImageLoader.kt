package com.pghaz.revery.image

import android.widget.ImageView
import com.pghaz.revery.R
import com.pghaz.revery.util.ViewUtils
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import jp.wasabeef.picasso.transformations.BlurTransformation
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation

class PicassoImageLoader : IImageLoader {

    private val picasso = Picasso.get()

    private var url: String? = null
    private var placeholderResId: Int? = null
    private var defaultPlaceholderResId: Int = R.drawable.placeholder_square

    private var transformations = ArrayList<Transformation>()

    private var width: Int = 0
    private var ratio = -1f

    private var centerCrop = false
    private var blur = false

    override fun load(url: String?): IImageLoader {
        this.url = url
        return this
    }

    override fun placeholder(placeholderResId: Int): IImageLoader {
        this.placeholderResId = placeholderResId
        return this
    }

    override fun ratioAndWidth(ratio: Float, width: Int, centerCrop: Boolean): IImageLoader {
        this.ratio = ratio
        this.width = width
        this.centerCrop = centerCrop
        return this
    }

    override fun roundCorners(radiusDp: Int, marginsDp: Int): IImageLoader {
        val transformation = RoundedCornersTransformation(ViewUtils.dpToPx(radiusDp), marginsDp)

        if (!transformations.contains(transformation)) {
            transformations.add(transformation)
        }

        return this
    }

    override fun blur(): IImageLoader {
        this.blur = true
        return this
    }

    override fun into(imageView: ImageView?) {
        val requestCreator = picasso.load(url)

        placeholderResId?.let {
            requestCreator.placeholder(it)
        } ?: run {
            requestCreator.placeholder(defaultPlaceholderResId)
        }

        transformations.forEach {
            requestCreator.transform(it)
        }

        if (blur) {
            requestCreator.transform(BlurTransformation(imageView?.context))
        }

        if (ratio != -1f) {
            val size = imageView?.let { ViewUtils.getSize(it, ratio, width) }
            size?.let {
                requestCreator.resize(it.first!!, it.second!!)
            }

            if (centerCrop) {
                requestCreator.centerCrop()
            }
        }

        requestCreator.into(imageView)
    }
}