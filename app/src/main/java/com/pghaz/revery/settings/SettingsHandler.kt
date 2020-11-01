package com.pghaz.revery.settings

import android.content.Context
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.net.Uri

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

    private const val SETTINGS_ALARM_VOLUME = "$SETTINGS_SHARED_PREF.alarm.volume"

    // Default alarm
    private const val SETTINGS_ALARM_DEFAULT_AUDIO_URI =
        "$SETTINGS_SHARED_PREF.alarm.default.audio.uri"
    private val DEFAULT_ALARM_DEFAULT_AUDIO_URI =
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString()

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

    fun setDefaultAudioUri(context: Context, defaultAudioUri: Uri) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putString(SETTINGS_ALARM_DEFAULT_AUDIO_URI, defaultAudioUri.toString())
        editor.apply()
    }

    fun getDefaultAudioUri(context: Context): Uri {
        val sharedPreferences = getSharedPreferences(context)
        return Uri.parse(
            sharedPreferences.getString(
                SETTINGS_ALARM_DEFAULT_AUDIO_URI,
                DEFAULT_ALARM_DEFAULT_AUDIO_URI
            ) ?: DEFAULT_ALARM_DEFAULT_AUDIO_URI
        )
    }

    fun getAlarmVolume(context: Context, defaultVolume: Int): Int {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getInt(SETTINGS_ALARM_VOLUME, defaultVolume)
    }

    fun setAlarmVolume(context: Context, volume: Int) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putInt(SETTINGS_ALARM_VOLUME, volume)
        editor.apply()
    }
}