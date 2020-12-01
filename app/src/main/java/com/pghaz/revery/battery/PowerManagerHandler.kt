package com.pghaz.revery.battery

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.os.PowerManager
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import com.pghaz.revery.BuildConfig
import com.pghaz.revery.R

object PowerManagerHandler {

    const val REQUEST_CODE_POWER_MANAGER_PROTECTED_APPS = 66
    const val REQUEST_CODE_POWER_MANAGER_BATTERY_OPTIMIZATION = 67

    fun updateGeneralViews(messageTextView: AppCompatTextView) {
        messageTextView.text = String.format(
            messageTextView.context.getString(R.string.android_settings_battery_optimization_message),
            messageTextView.context.getString(R.string.app_name)
        )
    }

    fun updateActionButtonText(
        isFirstTime: Boolean,
        hasAutoStartFeature: Boolean,
        isIgnoringBatteryOptimizations: Boolean, goToSettingsButton: AppCompatButton
    ) {
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

        goToSettingsButton.text =
            String.format(
                "%s (%s)",
                goToSettingsButton.context.getString(R.string.go_to_settings),
                stepText
            )
    }

    fun updateAutoStartViews(
        context: Context,
        hasAutoStartFeature: Boolean,
        autoStartTextView: AppCompatTextView,
        autoStartErrorTextView: AppCompatTextView
    ) {
        if (hasAutoStartFeature) {
            autoStartTextView.text = String.format(
                context.getString(R.string.android_settings_auto_start_requirement),
                context.getString(R.string.app_name)
            )

            autoStartTextView.visibility = View.VISIBLE
            autoStartErrorTextView.visibility = View.VISIBLE
        } else {
            autoStartTextView.visibility = View.GONE
            autoStartErrorTextView.visibility = View.GONE
        }
    }

    fun updateBatteryOptimizationViews(
        isIgnoringBatteryOptimizations: Boolean,
        batteryOptimizationTextView: AppCompatTextView
    ) {
        batteryOptimizationTextView.text = String.format(
            batteryOptimizationTextView.context.getString(R.string.android_settings_battery_optimization_requirement),
            batteryOptimizationTextView.context.getString(R.string.app_name)
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
    }

    fun getAutoStartFeatureIntent(context: Context): Intent? {
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
        val packageName = context.packageName

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return powerManager.isIgnoringBatteryOptimizations(packageName)
        }

        return true
    }
}