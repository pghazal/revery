package com.pghaz.revery.settings

import android.content.Context
import android.content.SharedPreferences

object SettingsHandler {

    private const val SETTINGS_SHARED_PREF = "com.pghaz.revery.settings"
    private const val SETTINGS_SNOOZE_DURATION = "com.pghaz.revery.settings.snooze_duration"
    private const val SETTINGS_SNOOZE_DURATION_POSITION =
        "com.pghaz.revery.settings.snooze_duration.position"
    private const val SETTINGS_FADE_IN = "com.pghaz.revery.settings.fade_in"
    private const val SETTINGS_FADE_IN_POSITION = "com.pghaz.revery.settings.fade_in.position"

    private val DEFAULT_SNOOZE_DURATION = SnoozeDuration.TEN_MINUTES
    private val DEFAULT_FADE_IN_DURATION = FadeInDuration.THIRTY_SECONDS

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
}