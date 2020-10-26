package com.pghaz.revery.image

import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.squareup.picasso.Transformation

interface IImageLoader {

    fun load(url: String?): IImageLoader

    fun placeholder(@DrawableRes placeholderResId: Int): IImageLoader

    fun transform(transformation: Transformation): IImageLoader

    fun into(imageView: ImageView?)
}