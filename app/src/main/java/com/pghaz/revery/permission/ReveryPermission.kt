package com.pghaz.revery.permission

import android.Manifest

enum class ReveryPermission(val systemPermissions: Array<String>, val requestCode: Int) {

    WRITE_EXTERNAL_STORAGE(
        arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE),
        RequestCode.REQUEST_CODE_WRITE_EXTERNAL_STORAGE
    );

    // http://stackoverflow.com/a/33331459/2122876
    // Request codes are now 16 bits wide : 0 to 65535 (were 8 bits wise)
    object RequestCode {
        const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 13
    }
}