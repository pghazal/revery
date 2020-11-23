package com.pghaz.revery.util

import android.content.Intent
import android.os.Bundle
import com.pghaz.revery.model.app.Alarm
import com.pghaz.revery.model.app.StandByEnabler
import com.pghaz.revery.model.app.Timer

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

    fun safePutTimerIntoIntent(intent: Intent, timer: Timer) {
        val timerBundle = getTimerBundle(timer)
        intent.putExtra(Arguments.ARGS_BUNDLE_TIMER, timerBundle)
    }

    fun safeGetTimerFromIntent(intent: Intent): Timer {
        val timerBundle = intent.getBundleExtra(Arguments.ARGS_BUNDLE_TIMER)
        return timerBundle?.getParcelable<Timer>(Arguments.ARGS_TIMER) as Timer
    }

    fun safePutTimerIntoBundle(bundle: Bundle, timer: Timer) {
        val timerBundle = getTimerBundle(timer)
        bundle.putBundle(Arguments.ARGS_BUNDLE_TIMER, timerBundle)
    }

    fun safeGetTimerFromBundle(bundle: Bundle?): Timer {
        val timerBundle = bundle?.getBundle(Arguments.ARGS_BUNDLE_TIMER)
        return timerBundle?.getParcelable<Timer>(Arguments.ARGS_TIMER) as Timer
    }

    private fun getTimerBundle(timer: Timer): Bundle {
        val timerBundle = Bundle()
        timerBundle.putParcelable(Arguments.ARGS_TIMER, timer)
        return timerBundle
    }

    fun safePutStandByEnablerIntoIntent(intent: Intent, standByEnabler: StandByEnabler) {
        val bundle = getStandByEnablerBundle(standByEnabler)
        intent.putExtra(Arguments.ARGS_BUNDLE_STANDBY, bundle)
    }

    fun safeGetStandByEnablerFromIntent(intent: Intent): StandByEnabler {
        val bundle = intent.getBundleExtra(Arguments.ARGS_BUNDLE_STANDBY)
        return bundle?.getParcelable<StandByEnabler>(Arguments.ARGS_STANDBY) as StandByEnabler
    }

    private fun getStandByEnablerBundle(standByEnabler: StandByEnabler): Bundle {
        val bundle = Bundle()
        bundle.putParcelable(Arguments.ARGS_STANDBY, standByEnabler)
        return bundle
    }
}