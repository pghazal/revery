package com.pghaz.revery.image

import android.content.Context
import android.widget.ImageView
import androidx.annotation.DrawableRes

interface IImageLoader {

    fun load(url: String?): IImageLoader

    fun placeholder(@DrawableRes placeholderResId: Int): IImageLoader

    fun into(imageView: ImageView?)

    fun ratioAndWidth(ratio: Float, width: Int, centerCrop: Boolean): IImageLoader

    fun roundCorners(radiusDp: Int, marginsDp: Int): IImageLoader

    fun blur(): IImageLoader
}