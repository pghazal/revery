package com.pghaz.revery.battery

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatTextView
import com.pghaz.revery.R

object PowerManagerHandler {

    private const val POWER_MANAGER_SHARED_PREF = "com.pghaz.revery.power_manager"
    private const val POWER_MANAGER_SKIP_CHECK = "${POWER_MANAGER_SHARED_PREF}.skip_check"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(
            POWER_MANAGER_SHARED_PREF,
            Context.MODE_PRIVATE
        )
    }

    private fun shouldSkipPowerManagerCheck(sharedPreferences: SharedPreferences): Boolean {
        return sharedPreferences.getBoolean(POWER_MANAGER_SKIP_CHECK, false)
    }

    private fun setShouldSkipPowerManagerCheck(
        sharedPreferences: SharedPreferences,
        skip: Boolean
    ) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(POWER_MANAGER_SKIP_CHECK, skip)
        editor.apply()
    }

    fun startPowerSaverIntent(context: Context) {
        val sharedPreferences = getSharedPreferences(context)
        val shouldSkipDialogCheck = shouldSkipPowerManagerCheck(sharedPreferences)

        if (!shouldSkipDialogCheck) {
            var foundCorrectIntent = false

            for (intent in PowerManagerManufacturerConstants.POWER_MANAGER_INTENTS) {
                if (isCallable(context, intent)) {
                    foundCorrectIntent = true

                    val customView = LayoutInflater.from(context)
                        .inflate(R.layout.dialog_protected_apps, null)
                    customView.findViewById<AppCompatTextView>(R.id.titleTextView).text =
                        String.format(
                            "%s %s",
                            Build.MANUFACTURER,
                            context.getString(R.string.protected_apps)
                        )
                    customView.findViewById<AppCompatTextView>(R.id.messageTextView).text =
                        String.format(
                            "%s %s",
                            context.getString(R.string.app_name),
                            context.getString(R.string.protected_apps_message)
                        )
                    customView.findViewById<AppCompatCheckBox>(R.id.doNotShowAgainCheckBox)
                        .setOnCheckedChangeListener { _, isChecked ->
                            setShouldSkipPowerManagerCheck(sharedPreferences, isChecked)
                        }

                    val dialog = AlertDialog.Builder(context)
                        .setView(customView)
                        .create()

                    customView.findViewById<AppCompatButton>(R.id.goToSettingsButton)
                        .setOnClickListener {
                            dialog.dismiss()
                            context.startActivity(intent)
                        }

                    dialog.show()

                    break
                }
            }

            if (!foundCorrectIntent) {
                setShouldSkipPowerManagerCheck(sharedPreferences, true)
            }
        }
    }

    private fun isCallable(context: Context?, intent: Intent?): Boolean {
        return try {
            if (intent == null || context == null) {
                false
            } else {
                val list: List<ResolveInfo> = context.packageManager.queryIntentActivities(
                    intent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                list.isNotEmpty()
            }
        } catch (ignored: Exception) {
            false
        }
    }
}