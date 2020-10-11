package com.pghaz.revery.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class ReveryImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    override fun setEnabled(enabled: Boolean) {
        if (this.isEnabled != enabled) {
            this.imageAlpha = if (enabled) 0xFF else 0x80
        }
        super.setEnabled(enabled)
    }
}