package com.pghaz.revery.battery

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import com.pghaz.revery.BaseFragment
import com.pghaz.revery.LauncherForResultComponent
import com.pghaz.revery.R
import com.pghaz.revery.notification.NotificationHandler
import kotlinx.android.synthetic.main.fragment_power_settings_android.*

open class PowerSettingsFragment : BaseFragment(), LauncherForResultComponent {

    companion object {
        const val TAG = "PowerSettingsFragment"
        const val REQUEST_CODE_DO_NOT_DISTURB = 77
    }

    private var isFirstTime: Boolean = true
    private var isIgnoringBatteryOptimizations: Boolean = false
    private var isNotificationPolicyAccessGranted: Boolean = false
    private lateinit var notificationManager: NotificationManager
    private lateinit var batteryOptimizationIntent: Intent
    private var autoStartIntent: Intent? = null
    private var hasAutoStartFeature: Boolean = false
    private var requestCode: Int = PowerManagerHandler.REQUEST_CODE_POWER_MANAGER_PROTECTED_APPS

    override fun getLayoutResId(): Int {
        return R.layout.fragment_power_settings_android
    }

    override fun launchActivityForResult(intent: Intent?, requestCode: Int) {
        startActivityForResult(intent, requestCode)
    }

    open fun isOpenedFromSettings(): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        context?.let {
            isIgnoringBatteryOptimizations = PowerManagerHandler.isIgnoringBatteryOptimizations(it)

            batteryOptimizationIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)

            autoStartIntent = PowerManagerHandler.getAutoStartFeatureIntent(it)
            hasAutoStartFeature = autoStartIntent != null

            notificationManager =
                it.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            isNotificationPolicyAccessGranted =
                NotificationHandler.isNotificationPolicyAccessGranted(notificationManager)
        }
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        context?.let {
            if (isOpenedFromSettings()) {
                description.visibility = View.GONE
                headingTextView.visibility = View.GONE
            } else {
                description.visibility = View.VISIBLE
                headingTextView.visibility = View.VISIBLE
            }

            // Do not Disturb
            if (isOpenedFromSettings() || !isNotificationPolicyAccessGranted) {
                doNotDisturbContainer.visibility = View.VISIBLE
            } else {
                doNotDisturbContainer.visibility = View.GONE
            }

            doNotDisturbButton.setOnClickListener { _ ->
                NotificationHandler.startDoNotDisturbActivity(
                    it,
                    this, REQUEST_CODE_DO_NOT_DISTURB
                )
            }

            // Power Manager and Battery
            if (!isNotificationPolicyAccessGranted && (hasAutoStartFeature || !isIgnoringBatteryOptimizations)) {
                oneLastThingView.visibility = View.VISIBLE
            } else {
                oneLastThingView.visibility = View.GONE
            }

            if (isOpenedFromSettings() || hasAutoStartFeature || !isIgnoringBatteryOptimizations) {
                batteryOptimizationContainer.visibility = View.VISIBLE
            } else {
                batteryOptimizationContainer.visibility = View.GONE
            }

            goToSettingsButton.setOnClickListener {
                var intent = when (requestCode) {
                    PowerManagerHandler.REQUEST_CODE_POWER_MANAGER_PROTECTED_APPS -> autoStartIntent
                    PowerManagerHandler.REQUEST_CODE_POWER_MANAGER_BATTERY_OPTIMIZATION -> batteryOptimizationIntent
                    else -> null
                }

                var finalRequestCode = requestCode
                if (finalRequestCode == PowerManagerHandler.REQUEST_CODE_POWER_MANAGER_PROTECTED_APPS && !hasAutoStartFeature) {
                    intent = batteryOptimizationIntent
                    finalRequestCode =
                        PowerManagerHandler.REQUEST_CODE_POWER_MANAGER_BATTERY_OPTIMIZATION
                }

                launchActivityForResult(
                    intent,
                    finalRequestCode
                )
            }

            updateViews(it)
        }
    }

    private fun updateViews(context: Context) {
        PowerManagerHandler.updateGeneralViews(messageTextView)

        PowerManagerHandler.updateAutoStartViews(
            context,
            hasAutoStartFeature,
            autoStartTextView,
            autoStartErrorTextView
        )

        PowerManagerHandler.updateBatteryOptimizationViews(
            isIgnoringBatteryOptimizations,
            batteryOptimizationTextView
        )

        PowerManagerHandler.updateActionButtonText(
            isFirstTime,
            hasAutoStartFeature,
            isIgnoringBatteryOptimizations,
            goToSettingsButton
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        context?.let {
            if (requestCode == PowerManagerHandler.REQUEST_CODE_POWER_MANAGER_PROTECTED_APPS ||
                requestCode == PowerManagerHandler.REQUEST_CODE_POWER_MANAGER_BATTERY_OPTIMIZATION
            ) {
                isIgnoringBatteryOptimizations =
                    PowerManagerHandler.isIgnoringBatteryOptimizations(it)
                isFirstTime = false
                this.requestCode =
                    PowerManagerHandler.REQUEST_CODE_POWER_MANAGER_BATTERY_OPTIMIZATION
                updateViews(it)
            } else if (requestCode == REQUEST_CODE_DO_NOT_DISTURB) {
                isNotificationPolicyAccessGranted =
                    NotificationHandler.isNotificationPolicyAccessGranted(notificationManager)
                if (isNotificationPolicyAccessGranted) {
                    Toast.makeText(
                        it,
                        doNotDisturbButton.context.getString(R.string.on_boarding_android_authorize_do_not_disturb_success),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        it,
                        doNotDisturbButton.context.getString(R.string.on_boarding_android_authorize_do_not_disturb_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}