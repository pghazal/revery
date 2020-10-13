package com.pghaz.revery.image

import android.content.Context

class ImageLoader {

    companion object {
        fun get(): IImageLoader {
            return PicassoImageLoader()
        }
    }
}