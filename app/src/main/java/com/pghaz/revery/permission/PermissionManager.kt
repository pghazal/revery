package com.pghaz.revery.permission

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

object PermissionManager {

    private const val PERMISSIONS_SHARED_PREF = "com.pghaz.revery.permissions"

    private const val PERMISSIONS_STORAGE_ASKED = "$PERMISSIONS_SHARED_PREF.storage_asked"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(
            PERMISSIONS_SHARED_PREF,
            Context.MODE_PRIVATE
        )
    }

    fun setStoragePermissionHasBeenAsked(context: Context, asked: Boolean) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putBoolean(PERMISSIONS_STORAGE_ASKED, asked)
        editor.apply()
    }

    private fun getStoragePermissionHasBeenAsked(context: Context): Boolean {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getBoolean(
            PERMISSIONS_STORAGE_ASKED,
            false
        )
    }

    fun askForPermission(activity: Activity, permission: ReveryPermission) {
        ActivityCompat.requestPermissions(
            activity,
            permission.systemPermissions,
            permission.requestCode
        )
    }

    fun askForPermission(fragment: Fragment, permission: ReveryPermission) {
        fragment.requestPermissions(
            permission.systemPermissions,
            permission.requestCode
        )
    }

    fun hasPermissionBeenGranted(context: Context, permission: ReveryPermission): Boolean {
        for (systemPermission in permission.systemPermissions) {
            if (!hasPermissionBeenGranted(context, systemPermission)) {
                return false
            }
        }
        return true
    }

    private fun hasPermissionBeenGranted(context: Context, systemPermission: String): Boolean {
        return (ContextCompat.checkSelfPermission(context.applicationContext, systemPermission)
                == PackageManager.PERMISSION_GRANTED)
    }

    private fun hasDeniedForever(
        activity: Activity,
        systemPermission: String,
        hasBeenAsked: Boolean
    ): Boolean {
        return hasBeenAsked && !ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            systemPermission
        )
    }

    fun isBlocked(activity: Activity, permission: ReveryPermission): Boolean {
        for (systemPermission in permission.systemPermissions) {
            val hasBeenAsked = hasBeenAsked(activity)
            if (!hasDeniedForever(activity, systemPermission, hasBeenAsked)) {
                return false
            }
        }
        return true
    }

    private fun hasBeenAsked(context: Context): Boolean {
        return getStoragePermissionHasBeenAsked(context)
    }
}