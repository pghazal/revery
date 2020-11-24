package com.pghaz.revery.viewmodel.standby

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pghaz.revery.model.app.StandByEnabler
import com.pghaz.revery.settings.FadeDuration

class StandByViewModel : ViewModel() {

    companion object {
        private const val STANDBY_SHARED_PREF = "com.pghaz.revery.standby"

        private const val STANDBY_FEATURE_ENABLED = "$STANDBY_SHARED_PREF.feature.enabled"
        private const val DEFAULT_STANDBY_FEATURE_ENABLED = false

        private const val STANDBY_FEATURE_HOUR = "$STANDBY_SHARED_PREF.feature.hour"
        private const val DEFAULT_STANDBY_FEATURE_HOUR = 0

        private const val STANDBY_FEATURE_MINUTE = "$STANDBY_SHARED_PREF.feature.minute"
        private const val DEFAULT_STANDBY_FEATURE_MINUTE = 0

        private const val STANDBY_FEATURE_FADE_OUT = "$STANDBY_SHARED_PREF.feature.fade.out"
        private const val DEFAULT_STANDBY_FEATURE_FADE_OUT = false

        private const val STANDBY_FEATURE_FADE_OUT_DURATION =
            "$STANDBY_SHARED_PREF.feature.fade.out.duration"
        private const val STANDBY_FEATURE_FADE_OUT_DURATION_POSITION =
            "$STANDBY_SHARED_PREF.feature.fade.out.duration.position"
        private val DEFAULT_STANDBY_FEATURE_FADE_IN_DURATION = FadeDuration.ONE_MINUTE
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
            standByEnabler.fadeOut = getStandByFadeOut(it)
            standByEnabler.fadeOutDuration = getStandByFadeOutDuration(it)
        }

        return standByEnabler
    }

    private fun getStandByEnabled(context: Context): Boolean {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getBoolean(
            STANDBY_FEATURE_ENABLED,
            DEFAULT_STANDBY_FEATURE_ENABLED
        )
    }

    fun setStandByEnabled(context: Context, value: Boolean) {
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

    fun setStandByHour(context: Context, value: Int) {
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

    fun setStandByMinute(context: Context, value: Int) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putInt(STANDBY_FEATURE_MINUTE, value)
        editor.apply()
    }

    private fun getStandByFadeOut(context: Context): Boolean {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getBoolean(
            STANDBY_FEATURE_FADE_OUT,
            DEFAULT_STANDBY_FEATURE_FADE_OUT
        )
    }

    fun setStandByFadeOut(context: Context, value: Boolean) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putBoolean(STANDBY_FEATURE_FADE_OUT, value)
        editor.apply()
    }

    private fun getStandByFadeOutDuration(context: Context): Long {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getLong(
            STANDBY_FEATURE_FADE_OUT_DURATION,
            DEFAULT_STANDBY_FEATURE_FADE_IN_DURATION.seconds
        )
    }

    fun setStandByFadeOutDuration(context: Context, fadeDuration: FadeDuration) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putLong(STANDBY_FEATURE_FADE_OUT_DURATION, fadeDuration.seconds)
        editor.putInt(STANDBY_FEATURE_FADE_OUT_DURATION_POSITION, fadeDuration.ordinal)
        editor.apply()
    }

    fun getStandByFadeOutDurationPosition(context: Context): Int {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getInt(
            STANDBY_FEATURE_FADE_OUT_DURATION_POSITION,
            DEFAULT_STANDBY_FEATURE_FADE_IN_DURATION.ordinal
        )
    }
}