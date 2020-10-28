package com.pghaz.revery.settings

import android.content.Context
import android.content.SharedPreferences

object SettingsHandler {

    private const val SETTINGS_SHARED_PREF = "com.pghaz.revery.settings"

    // Snooze
    private const val SETTINGS_SNOOZE_DURATION = "$SETTINGS_SHARED_PREF.snooze_duration"
    private const val SETTINGS_SNOOZE_DURATION_POSITION =
        "$SETTINGS_SHARED_PREF.snooze_duration.position"
    private val DEFAULT_SNOOZE_DURATION = SnoozeDuration.TEN_MINUTES

    // Fade in
    private const val SETTINGS_FADE_IN = "$SETTINGS_SHARED_PREF.fade_in"
    private const val SETTINGS_FADE_IN_POSITION = "$SETTINGS_SHARED_PREF.fade_in.position"
    private val DEFAULT_FADE_IN_DURATION = FadeInDuration.THIRTY_SECONDS

    // Volume
    // Should use user device volume or the max possible
    private const val SETTINGS_SHOULD_USE_DEVICE_VOLUME =
        "$SETTINGS_SHARED_PREF.alarm.volume.device"
    private const val DEFAULT_SHOULD_USE_DEVICE_VOLUME = true

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(
            SETTINGS_SHARED_PREF,
            Context.MODE_PRIVATE
        )
    }

    fun setSnoozeDuration(context: Context, snoozeDuration: SnoozeDuration) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putInt(SETTINGS_SNOOZE_DURATION, snoozeDuration.minutes)
        editor.putInt(SETTINGS_SNOOZE_DURATION_POSITION, snoozeDuration.ordinal)
        editor.apply()
    }

    fun getSnoozeDuration(context: Context): Int {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getInt(SETTINGS_SNOOZE_DURATION, DEFAULT_SNOOZE_DURATION.minutes)
    }

    fun getSnoozeDurationPosition(context: Context): Int {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getInt(
            SETTINGS_SNOOZE_DURATION_POSITION,
            DEFAULT_SNOOZE_DURATION.ordinal
        )
    }

    fun setFadeInDuration(context: Context, fadeInDuration: FadeInDuration) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putLong(SETTINGS_FADE_IN, fadeInDuration.seconds)
        editor.putInt(SETTINGS_FADE_IN_POSITION, fadeInDuration.ordinal)
        editor.apply()
    }

    fun getFadeInDuration(context: Context): Long {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getLong(SETTINGS_FADE_IN, DEFAULT_FADE_IN_DURATION.seconds)
    }

    fun getFadeInDurationPosition(context: Context): Int {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getInt(
            SETTINGS_FADE_IN_POSITION,
            DEFAULT_FADE_IN_DURATION.ordinal
        )
    }

    fun setShouldUseDeviceVolume(context: Context, userVolume: Boolean) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putBoolean(SETTINGS_SHOULD_USE_DEVICE_VOLUME, userVolume)
        editor.apply()
    }

    fun getShouldUseDeviceVolume(context: Context): Boolean {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getBoolean(
            SETTINGS_SHOULD_USE_DEVICE_VOLUME,
            DEFAULT_SHOULD_USE_DEVICE_VOLUME
        )
    }
}