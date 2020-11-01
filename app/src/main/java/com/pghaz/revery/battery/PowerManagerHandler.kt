package com.pghaz.revery.battery

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatTextView
import com.pghaz.revery.BuildConfig
import com.pghaz.revery.R

object PowerManagerHandler {

    private const val POWER_MANAGER_SHARED_PREF = "com.pghaz.revery.power_manager"
    private const val KEY_POWER_MANAGER_SKIP_CHECK = "${POWER_MANAGER_SHARED_PREF}.skip_check"
    private const val KEY_OPENED_FROM_SETTINGS = "${POWER_MANAGER_SHARED_PREF}.opened_from_settings"

    const val REQUEST_CODE_POWER_MANAGER_PROTECTED_APPS = 66
    const val REQUEST_CODE_POWER_MANAGER_BATTERY_OPTIMIZATION = 67

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(
            POWER_MANAGER_SHARED_PREF,
            Context.MODE_PRIVATE
        )
    }

    private fun shouldSkipPowerManagerCheck(sharedPreferences: SharedPreferences): Boolean {
        return sharedPreferences.getBoolean(KEY_POWER_MANAGER_SKIP_CHECK, false)
    }

    private fun setShouldSkipPowerManagerCheck(
        sharedPreferences: SharedPreferences,
        skip: Boolean
    ) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_POWER_MANAGER_SKIP_CHECK, skip)
        editor.apply()
    }

    fun getOpenedFromSettings(context: Context): Boolean {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getBoolean(KEY_OPENED_FROM_SETTINGS, false)
    }

    private fun setOpenedFromSettings(sharedPreferences: SharedPreferences, value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_OPENED_FROM_SETTINGS, value)
        editor.apply()
    }

    fun showPowerSaverDialogIfNeeded(
        activity: Activity,
        requestCode: Int,
        isFirstTime: Boolean,
        openingFromSettings: Boolean
    ) {
        if (requestCode != REQUEST_CODE_POWER_MANAGER_PROTECTED_APPS &&
            requestCode != REQUEST_CODE_POWER_MANAGER_BATTERY_OPTIMIZATION
        ) {
            throw IllegalArgumentException("Wrong requestCode in PowerManagerHandler")
        }

        val sharedPreferences = getSharedPreferences(activity)
        val shouldSkipDialogCheck = shouldSkipPowerManagerCheck(sharedPreferences)

        setOpenedFromSettings(sharedPreferences, openingFromSettings)

        if (openingFromSettings || !shouldSkipDialogCheck) {
            val isIgnoringBatteryOptimizations = isIgnoringBatteryOptimizations(activity)
            val batteryOptimizationIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    addCategory(Intent.CATEGORY_DEFAULT)
                    data = Uri.parse("package:${activity.packageName}")
                }
            } else {
                Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            }

            val autoStartIntent = getAutoStartFeatureIntent(activity)
            val hasAutoStartFeature = autoStartIntent != null
            if (isIgnoringBatteryOptimizations && !openingFromSettings) {
                setShouldSkipPowerManagerCheck(sharedPreferences, true)
                return
            }

            var intent = when (requestCode) {
                REQUEST_CODE_POWER_MANAGER_PROTECTED_APPS -> autoStartIntent
                REQUEST_CODE_POWER_MANAGER_BATTERY_OPTIMIZATION -> batteryOptimizationIntent
                else -> null
            }

            var finalRequestCode = requestCode
            if (finalRequestCode == REQUEST_CODE_POWER_MANAGER_PROTECTED_APPS && !hasAutoStartFeature) {
                intent = batteryOptimizationIntent
                finalRequestCode = REQUEST_CODE_POWER_MANAGER_BATTERY_OPTIMIZATION
            }

            intent?.putExtra(KEY_OPENED_FROM_SETTINGS, openingFromSettings)

            showDialog(
                activity,
                sharedPreferences,
                openingFromSettings,
                shouldSkipDialogCheck,
                isFirstTime,
                hasAutoStartFeature,
                isIgnoringBatteryOptimizations,
                intent,
                finalRequestCode
            )
        }
    }

    private fun showDialog(
        activity: Activity,
        sharedPreferences: SharedPreferences,
        openingFromSettings: Boolean,
        shouldSkipDialogCheck: Boolean,
        isFirstTime: Boolean,
        hasAutoStartFeature: Boolean,
        isIgnoringBatteryOptimizations: Boolean,
        intent: Intent?,
        requestCode: Int
    ) {

        val customView = LayoutInflater.from(activity)
            .inflate(R.layout.dialog_battery_optimization, null)

        // Title
        customView.findViewById<AppCompatTextView>(R.id.titleTextView).text =
            String.format(
                activity.getString(R.string.battery_optimization_title)
            )

        // Message
        customView.findViewById<AppCompatTextView>(R.id.messageTextView).text =
            String.format(
                activity.getString(R.string.battery_optimization_message),
                activity.getString(R.string.app_name)
            )

        // Reshow Dialog text
        val reshowDialogTextView =
            customView.findViewById<AppCompatTextView>(R.id.reshowDialogTextView)
        reshowDialogTextView.text =
            String.format(
                activity.getString(R.string.battery_optimization_reshow_dialog),
                activity.getString(R.string.app_name)
            )

        if (openingFromSettings) {
            reshowDialogTextView.visibility = View.GONE
        } else {
            reshowDialogTextView.visibility = View.VISIBLE
        }

        // AutoStart feature
        val autoStartTextView =
            customView.findViewById<AppCompatTextView>(R.id.autoStartTextView)
        val autoStartErrorTextView =
            customView.findViewById<AppCompatTextView>(R.id.autoStartErrorTextView)

        if (hasAutoStartFeature) {
            autoStartTextView.text = String.format(
                activity.getString(R.string.battery_auto_start_requirement),
                activity.getString(R.string.app_name)
            )

            autoStartTextView.visibility = View.VISIBLE
            autoStartErrorTextView.visibility = View.VISIBLE

            if (isFirstTime) {
                autoStartTextView.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_feature_disabled,
                    0,
                    0,
                    0
                )
            } else {
                autoStartTextView.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_feature_enabled,
                    0,
                    0,
                    0
                )
            }
        } else {
            autoStartTextView.visibility = View.GONE
            autoStartErrorTextView.visibility = View.GONE
        }

        // Battery optim
        val batteryOptimizationTextView =
            customView.findViewById<AppCompatTextView>(R.id.batteryOptimizationTextView)
        batteryOptimizationTextView.text = String.format(
            activity.getString(R.string.battery_optimization_requirement),
            activity.getString(R.string.app_name)
        )

        if (isIgnoringBatteryOptimizations) {
            batteryOptimizationTextView.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_feature_enabled,
                0,
                0,
                0
            )
        } else {
            batteryOptimizationTextView.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_feature_disabled,
                0,
                0,
                0
            )
        }

        // Do not show again
        val doNotShowAgainCheckBox =
            customView.findViewById<AppCompatCheckBox>(R.id.doNotShowAgainCheckBox)
        doNotShowAgainCheckBox.setOnCheckedChangeListener { _, isChecked ->
            setShouldSkipPowerManagerCheck(sharedPreferences, isChecked)
        }

        if (shouldSkipDialogCheck) {
            doNotShowAgainCheckBox.visibility = View.GONE
        } else {
            doNotShowAgainCheckBox.visibility = View.VISIBLE
        }

        val dialog = AlertDialog.Builder(activity)
            .setView(customView)
            .create()

        val stepText =
            if (!isFirstTime && !hasAutoStartFeature && !isIgnoringBatteryOptimizations) {
                "0/1"
            } else if (isFirstTime && hasAutoStartFeature && !isIgnoringBatteryOptimizations) {
                "0/2"
            } else if (isFirstTime && !hasAutoStartFeature && !isIgnoringBatteryOptimizations) {
                "0/1"
            } else if (!isFirstTime && hasAutoStartFeature && !isIgnoringBatteryOptimizations) {
                "1/2"
            } else if (!isFirstTime && !hasAutoStartFeature && isIgnoringBatteryOptimizations) {
                "1/1"
            } else if (isFirstTime && !hasAutoStartFeature && isIgnoringBatteryOptimizations) {
                "1/1"
            } else if (!isFirstTime && hasAutoStartFeature && isIgnoringBatteryOptimizations) {
                "2/2"
            } else {
                "2/2"
            }

        // Action Button
        val goToSettingsButton = customView.findViewById<AppCompatButton>(R.id.goToSettingsButton)
        goToSettingsButton.text =
            String.format("%s (%s)", goToSettingsButton.text.toString(), stepText)
        goToSettingsButton.setOnClickListener {
            dialog.dismiss()
            activity.startActivityForResult(
                intent,
                requestCode
            )
        }

        customView.findViewById<AppCompatButton>(R.id.closeButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun getAutoStartFeatureIntent(context: Context): Intent? {
        for (intent in PowerManagerManufacturerConstants.POWER_MANAGER_INTENTS) {
            if (isCallable(context, intent)) {
                return intent
            }
        }

        return null
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

    /**
     * return true if in App's Battery settings "Not optimized" and false if "Optimizing battery use"
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager =
            context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val packageName = BuildConfig.APPLICATION_ID

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return powerManager.isIgnoringBatteryOptimizations(packageName)
        }

        return true
    }
}