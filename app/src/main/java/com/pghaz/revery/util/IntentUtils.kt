package com.pghaz.revery.util

import android.content.Intent
import android.os.Bundle
import com.pghaz.revery.alarm.model.app.AbstractAlarm

object IntentUtils {

    fun safePutAlarmIntoIntent(intent: Intent, alarm: AbstractAlarm) {
        val alarmBundle = getAlarmBundle(alarm)
        intent.putExtra(Arguments.ARGS_BUNDLE_ALARM, alarmBundle)
    }

    fun safeGetAlarmFromIntent(intent: Intent): AbstractAlarm {
        val alarmBundle = intent.getBundleExtra(Arguments.ARGS_BUNDLE_ALARM)
        return alarmBundle?.getParcelable<AbstractAlarm>(Arguments.ARGS_ALARM) as AbstractAlarm
    }

    fun safePutAlarmIntoBundle(bundle: Bundle, alarm: AbstractAlarm) {
        val alarmBundle = getAlarmBundle(alarm)
        bundle.putBundle(Arguments.ARGS_BUNDLE_ALARM, alarmBundle)
    }

    fun safeGetAlarmFromBundle(bundle: Bundle?): AbstractAlarm {
        val alarmBundle = bundle?.getBundle(Arguments.ARGS_BUNDLE_ALARM)
        return alarmBundle?.getParcelable<AbstractAlarm>(Arguments.ARGS_ALARM) as AbstractAlarm
    }

    private fun getAlarmBundle(alarm: AbstractAlarm): Bundle {
        val alarmBundle = Bundle()
        alarmBundle.putParcelable(Arguments.ARGS_ALARM, alarm)
        return alarmBundle
    }
}