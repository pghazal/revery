package com.pghaz.revery

import android.content.Intent

interface LauncherForResultComponent {
    fun launchActivityForResult(intent: Intent?, requestCode: Int)
}