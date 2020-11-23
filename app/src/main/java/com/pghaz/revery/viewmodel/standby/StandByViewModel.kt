package com.pghaz.revery.viewmodel.standby

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pghaz.revery.model.app.StandByEnabler

class StandByViewModel : ViewModel() {

    companion object {
        private const val STANDBY_SHARED_PREF = "com.pghaz.revery.standby"

        private const val STANDBY_FEATURE_ENABLED = "$STANDBY_SHARED_PREF.feature.enabled"
        private const val DEFAULT_STANDBY_FEATURE_ENABLED = false

        private const val STANDBY_FEATURE_HOUR = "$STANDBY_SHARED_PREF.feature.hour"
        private const val DEFAULT_STANDBY_FEATURE_HOUR = 0

        private const val STANDBY_FEATURE_MINUTE = "$STANDBY_SHARED_PREF.feature.minute"
        private const val DEFAULT_STANDBY_FEATURE_MINUTE = 0
    }

    val standbyLiveData = MutableLiveData<StandByEnabler>()

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(
            STANDBY_SHARED_PREF,
            Context.MODE_PRIVATE
        )
    }

    fun getStandByEnabler(context: Context?): StandByEnabler {
        val standByEnabler = StandByEnabler()

        context?.let {
            standByEnabler.enabled = getStandByEnabled(it)
            standByEnabler.hour = getStandByHour(it)
            standByEnabler.minute = getStandByMinute(it)
        }

        return standByEnabler
    }

    fun setStandByEnabler(context: Context?, enabled: Boolean, hour: Int, minute: Int) {
        context?.let {
            setStandByEnabled(it, enabled)
            setStandByHour(it, hour)
            setStandByMinute(it, minute)
        }
    }

    private fun getStandByEnabled(context: Context): Boolean {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getBoolean(
            STANDBY_FEATURE_ENABLED,
            DEFAULT_STANDBY_FEATURE_ENABLED
        )
    }

    private fun setStandByEnabled(context: Context, value: Boolean) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putBoolean(STANDBY_FEATURE_ENABLED, value)
        editor.apply()
    }

    private fun getStandByHour(context: Context): Int {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getInt(
            STANDBY_FEATURE_HOUR,
            DEFAULT_STANDBY_FEATURE_HOUR
        )
    }

    private fun setStandByHour(context: Context, value: Int) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putInt(STANDBY_FEATURE_HOUR, value)
        editor.apply()
    }

    private fun getStandByMinute(context: Context): Int {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getInt(
            STANDBY_FEATURE_MINUTE,
            DEFAULT_STANDBY_FEATURE_MINUTE
        )
    }

    private fun setStandByMinute(context: Context, value: Int) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putInt(STANDBY_FEATURE_MINUTE, value)
        editor.apply()
    }
}