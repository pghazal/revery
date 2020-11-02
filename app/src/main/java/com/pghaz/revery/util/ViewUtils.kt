package com.pghaz.revery.util

import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.util.DisplayMetrics
import android.view.Display
import android.view.View
import android.view.WindowManager
import androidx.core.util.Pair
import com.pghaz.revery.R

object ViewUtils {
    fun isTablet(context: Context): Boolean {
        return context.resources.getBoolean(R.bool.is_tablet)
    }

    fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

    fun pxToDp(px: Int): Int {
        return (px / Resources.getSystem().displayMetrics.density).toInt()
    }

    fun getRealPixelSize(size: Int, ctx: Context): Int {
        val lSize: Int
        val displayMetrics = ctx.resources.displayMetrics
        lSize = (size * displayMetrics.density).toInt()
        return lSize
    }

    fun getScreenWidthInPx(context: Context): Int {
        return getDisplayMetrics(context).widthPixels
    }

    fun getScreenHeightInPx(context: Context): Int {
        return getDisplayMetrics(context).heightPixels
    }

    /**
     * Useful when in Fullscreen mode.
     * This method returns the height of screen when Status bar and/or Navigation bar are hidden.
     *
     * @param context
     * @return height without substracting status bar or navigation bar height
     */
    fun getRealScreenHeightSize(context: Context): Int {
        val point = Point()
        getDisplay(context).getRealSize(point)
        return point.y
    }

    fun getRealScreenWidthSize(context: Context): Int {
        val point = Point()
        getDisplay(context).getRealSize(point)
        return point.x
    }

    fun getSize(view: View, ratio: Float, width: Int): Pair<Int, Int> {
        return if (ratio <= 0) {
            Pair(view.width, view.height)
        } else getSize(ratio, width)
    }

    fun getSize(ratio: Float, width: Int): Pair<Int, Int> {
        val computedHeight: Int
        val computedWidth: Int = width
        computedHeight = (computedWidth / ratio).toInt()
        return Pair(computedWidth, computedHeight)
    }

    private fun getDisplay(context: Context): Display {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return wm.defaultDisplay
    }

    private fun getDisplayMetrics(context: Context): DisplayMetrics {
        val displayMetrics = DisplayMetrics()
        getDisplay(context).getMetrics(displayMetrics)
        return displayMetrics
    }
}