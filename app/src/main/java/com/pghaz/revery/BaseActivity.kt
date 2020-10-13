package com.pghaz.revery

import android.content.Intent
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

abstract class BaseActivity : AppCompatActivity() {

    @LayoutRes
    abstract fun getLayoutResId(): Int
    abstract fun configureViews(savedInstanceState: Bundle?)
    abstract fun parseArguments(args: Bundle?)
    abstract fun shouldAnimateOnCreate(): Boolean
    abstract fun shouldAnimateOnFinish(): Boolean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!shouldAnimateOnCreate()) {
            overridePendingTransition(0, 0)
        }

        colorNavigationBar()

        setContentView(getLayoutResId())

        if (savedInstanceState != null) {
            parseArguments(savedInstanceState)
        } else {
            parseArguments(intent.extras)
        }

        configureViews(savedInstanceState)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        parseArguments(intent?.extras)
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

    fun replaceFragment(fragment: Fragment, tag: String) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment, tag)
            .commit()
    }
}