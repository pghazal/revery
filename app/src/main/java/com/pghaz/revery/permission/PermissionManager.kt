package com.pghaz.revery.permission

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

object PermissionManager {

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

    private fun hasDeniedForever(activity: Activity, systemPermission: String): Boolean {
        return !ActivityCompat.shouldShowRequestPermissionRationale(activity, systemPermission)
    }

    fun isBlocked(activity: Activity, permission: ReveryPermission): Boolean {
        for (syspermission in permission.systemPermissions) {
            if (!hasDeniedForever(activity, syspermission)) {
                return false
            }
        }
        return true
    }
}