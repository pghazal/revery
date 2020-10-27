package com.pghaz.revery.permission

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import com.pghaz.revery.R

object PermissionDialogFactory {

    fun showPermissionDialog(context: Context) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_permission_storage, null)
        val goToSettingsButton = view.findViewById<AppCompatButton>(R.id.goToSettingsButton)

        val builder = AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(true)

        val dialog = builder.create()

        goToSettingsButton.setOnClickListener {
            navigateToAppGeneralSettings(context)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun navigateToAppGeneralSettings(context: Context) {
        val myAppSettings = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:" + context.packageName)
        )
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT)
        myAppSettings.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(myAppSettings)
    }
}