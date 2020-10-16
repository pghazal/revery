package com.pghaz.revery.util

import android.content.Context
import com.pghaz.revery.R

object ViewUtils {
    fun isTablet(context: Context): Boolean {
        return context.resources.getBoolean(R.bool.is_tablet)
    }
}