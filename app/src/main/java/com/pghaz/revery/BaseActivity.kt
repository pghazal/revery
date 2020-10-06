package com.pghaz.revery

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

abstract class BaseActivity : AppCompatActivity() {

    @LayoutRes
    abstract fun getLayoutResId(): Int
    abstract fun configureViews(savedInstanceState: Bundle?)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        colorNavigationBar()

        setContentView(getLayoutResId())

        configureViews(savedInstanceState)
    }

    private fun colorNavigationBar() {
        window.navigationBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
    }
}