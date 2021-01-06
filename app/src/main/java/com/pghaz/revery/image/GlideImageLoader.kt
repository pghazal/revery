package com.pghaz.revery.image

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.pghaz.revery.R
import com.pghaz.revery.util.ViewUtils
import jp.wasabeef.glide.transformations.BitmapTransformation
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.RoundedCornersTransformation


class GlideImageLoader : IImageLoader {

    private var url: String? = null
    private var placeholderResId: Int? = null
    private var defaultPlaceholderResId: Int = R.drawable.placeholder_square

    private var transformations = ArrayList<BitmapTransformation>()

    private var width: Int = 0
    private var ratio = -1f

    private var centerCrop = false

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
        val transformation = BlurTransformation(25)

        if (!transformations.contains(transformation)) {
            transformations.add(transformation)
        }

        return this
    }

    override fun into(imageView: ImageView?) {
        imageView?.let {
            val requestManager = Glide.with(imageView.context)

            var options = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)

            placeholderResId?.let { resId ->
                options = options.placeholder(resId)
            } ?: run {
                options = options.placeholder(defaultPlaceholderResId)
            }

            if (centerCrop) {
                options = options.centerCrop()
            }

            if (ratio != -1f) {
                val size = ViewUtils.getSize(it, ratio, width)
                options = options.override(size.first!!, size.second!!)
            }

            if (transformations.isNotEmpty()) {
                val multiTransformation = MultiTransformation(transformations)
                options = options.transform(multiTransformation)
            }

            requestManager.load(url)
                .apply(options)
                .into(imageView)
        }
    }
}