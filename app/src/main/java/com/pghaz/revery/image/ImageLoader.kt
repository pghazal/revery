package com.pghaz.revery.image

class ImageLoader {

    companion object {
        fun get(): IImageLoader {
            return GlideImageLoader()
        }
    }
}