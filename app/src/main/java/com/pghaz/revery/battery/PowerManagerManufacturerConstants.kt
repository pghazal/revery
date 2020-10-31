package com.pghaz.revery.battery

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.pghaz.revery.BuildConfig

/**
 * Updated 26/06/2019
 *
 * Taken and updated from :
 * https://stackoverflow.com/questions/31638986/protected-apps-setting-on-huawei-phones-and-how-to-handle-it/49110392#49110392
 * https://github.com/judemanutd/AutoStarter
 *
 * These permissions need to be added in AndroidManifest.xml:
 *
 * <uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
 * <uses-permission android:name="oppo.permission.OPPO_COMPONENT_SAFE" />
 * <uses-permission android:name="com.huawei.permission.external_app_settings.USE_COMPONENT" />
 *
 */
object PowerManagerManufacturerConstants {

    @SuppressLint("BatteryLife")
    val POWER_MANAGER_INTENTS = listOf(

        /***
         * Xiaomi
         */
        Intent().setComponent(
            ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity"
            )
        ),

        /***
         * Letv
         */
        Intent().setComponent(
            ComponentName(
                "com.letv.android.letvsafe",
                "com.letv.android.letvsafe.AutobootManageActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.letv.android.letvsafe",
                "com.letv.android.letvsafe.AutobootManageActivity"
            )
        ).setData(Uri.parse("mobilemanager://function/entry/AutoStart")),

        /***
         * Honor
         */
        Intent().setComponent(
            ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.optimize.process.ProtectActivity"
            )
        ),

        /***
         * Huawei
         */
        Intent().setComponent(
            ComponentName(
                "com.huawei.systemmanager",
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                } else {
                    "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"
                }
            )
        ),

        /**
         * Oppo
         */
        Intent().setComponent(
            ComponentName(
                "com.oppo.safe",
                "com.oppo.safe.permission.startup.StartupAppListActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.coloros.oppoguardelf",
                "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.coloros.oppoguardelf",
                "com.coloros.powermanager.fuelgaue.PowerSaverModeActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.coloros.oppoguardelf",
                "com.coloros.powermanager.fuelgaue.PowerConsumptionActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.coloros.safecenter",
                "com.coloros.safecenter.permission.startup.StartupAppListActivity"
            )
        ),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Intent().setComponent(
                ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.startupapp.StartupAppListActivity"
                )
            ).setAction(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            ).setData(
                Uri.parse(
                    "package:" + BuildConfig.APPLICATION_ID
                )
            )
        } else {
            null
        },

        /**
         * Vivo
         */
        Intent().setComponent(
            ComponentName(
                "com.vivo.permissionmanager",
                "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.iqoo.secure",
                "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.iqoo.secure",
                "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"
            )
        ),

        /***
         * ASUS ROG
         */
        Intent().setComponent(
            ComponentName(
                "com.asus.mobilemanager",
                "com.asus.mobilemanager.entry.FunctionActivity"
            )
        ).setData(Uri.parse("mobilemanager://function/entry/AutoStart")),
        Intent().setComponent(
            ComponentName(
                "com.asus.mobilemanager",
                "com.asus.mobilemanager.autostart.AutoStartActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.asus.mobilemanager",
                "com.asus.mobilemanager.powersaver.PowerSaverSettings"
            )
        ),

        /**
         * Nokia
         */
        Intent().setComponent(
            ComponentName(
                "com.evenwell.powersaving.g3",
                "com.evenwell.powersaving.g3.exception.PowerSaverExceptionActivity"
            )
        ),

        /**
         * HTC
         */
        Intent().setComponent(
            ComponentName(
                "com.htc.pitroad",
                "com.htc.pitroad.landingpage.activity.LandingPageActivity"
            )
        ),

        /**
         * Samsung
         */
        Intent().setComponent(
            ComponentName(
                "com.samsung.android.lool",
                "com.samsung.android.sm.ui.battery.BatteryActivity"
            )
        ),

        /**
         * OnePlus
         */
        Intent().setComponent(
            ComponentName(
                "com.oneplus.security",
                "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
            )
        ),

        /**
         * Dewav
         */
        Intent().setComponent(
            ComponentName(
                "com.dewav.dwappmanager",
                "com.dewav.dwappmanager.memory.SmartClearupWhiteList"
            )
        ),

        /**
         * Meizu
         */
        Intent().setComponent(
            ComponentName(
                "com.meizu.safe",
                "com.meizu.safe.security.SHOW_APPSEC"
            )
        ).addCategory(Intent.CATEGORY_DEFAULT).putExtra("packageName", BuildConfig.APPLICATION_ID),
    )
}