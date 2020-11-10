package com.pghaz.revery.util

import android.content.Intent
import android.os.Bundle
import com.pghaz.revery.model.app.Alarm

object IntentUtils {

    fun safePutAlarmIntoIntent(intent: Intent, alarm: Alarm) {
        val alarmBundle = getAlarmBundle(alarm)
        intent.putExtra(Arguments.ARGS_BUNDLE_ALARM, alarmBundle)
    }

    fun safeGetAlarmFromIntent(intent: Intent): Alarm {
        val alarmBundle = intent.getBundleExtra(Arguments.ARGS_BUNDLE_ALARM)
        return alarmBundle?.getParcelable<Alarm>(Arguments.ARGS_ALARM) as Alarm
    }

    fun safePutAlarmIntoBundle(bundle: Bundle, alarm: Alarm) {
        val alarmBundle = getAlarmBundle(alarm)
        bundle.putBundle(Arguments.ARGS_BUNDLE_ALARM, alarmBundle)
    }

    fun safeGetAlarmFromBundle(bundle: Bundle?): Alarm {
        val alarmBundle = bundle?.getBundle(Arguments.ARGS_BUNDLE_ALARM)
        return alarmBundle?.getParcelable<Alarm>(Arguments.ARGS_ALARM) as Alarm
    }

    private fun getAlarmBundle(alarm: Alarm): Bundle {
        val alarmBundle = Bundle()
        alarmBundle.putParcelable(Arguments.ARGS_ALARM, alarm)
        return alarmBundle
    }
}