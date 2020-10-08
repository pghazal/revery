package com.pghaz.revery

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

abstract class BaseActivity : AppCompatActivity() {

    @LayoutRes
    abstract fun getLayoutResId(): Int
    abstract fun configureViews(savedInstanceState: Bundle?)
    abstract fun shouldAnimateOnCreate(): Boolean
    abstract fun shouldAnimateOnFinish(): Boolean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!shouldAnimateOnCreate()) {
            overridePendingTransition(0, 0)
        }

        colorNavigationBar()

        setContentView(getLayoutResId())

        configureViews(savedInstanceState)
    }

    private fun colorNavigationBar() {
        window.navigationBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
    }

    override fun finish() {
        super.finish()
        if (!shouldAnimateOnFinish()) {
            overridePendingTransition(0, 0)
        }
    }
}