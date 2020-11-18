package com.pghaz.revery.settings

import android.content.Context
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.net.Uri

object SettingsHandler {

    private const val SETTINGS_SHARED_PREF = "com.pghaz.revery.settings"

    // General
    private const val SETTINGS_LAST_OPENED_TAB = "$SETTINGS_SHARED_PREF.last.opened.tab"
    private val DEFAULT_LAST_OPENED_TAB = TabFeature.ALARM

    // On Boarding
    private const val SETTINGS_ON_BOARDING_SHOWN = "$SETTINGS_SHARED_PREF.on.boarding.shown"
    private const val DEFAULT_ON_BOARDING_SHOWN = false

    // Turn off
    private const val SETTINGS_SLIDE_TO_TURN_OFF = "$SETTINGS_SHARED_PREF.alarm.slide.to.turn.off"
    private const val DEFAULT_SLIDE_TO_TURN_OFF = false

    // Snooze
    private const val SETTINGS_SNOOZE_DURATION = "$SETTINGS_SHARED_PREF.snooze_duration"
    private const val SETTINGS_SNOOZE_DURATION_POSITION =
        "$SETTINGS_SHARED_PREF.snooze_duration.position"
    val DEFAULT_SNOOZE_DURATION = SnoozeDuration.TEN_MINUTES

    private const val SETTINGS_SNOOZE_CAN_CHANGE_DURATION =
        "$SETTINGS_SHARED_PREF.snooze.can.change.duration"
    private const val DEFAULT_SNOOZE_CAN_CHANGE_DURATION = false

    private const val SETTINGS_SNOOZE_DOUBLE_TAP =
        "$SETTINGS_SHARED_PREF.snooze.double_tap"
    private const val DEFAULT_SNOOZE_DOUBLE_TAP = false

    // Fade in
    private const val SETTINGS_FADE_IN = "$SETTINGS_SHARED_PREF.fade_in"
    private const val SETTINGS_FADE_IN_POSITION = "$SETTINGS_SHARED_PREF.fade_in.position"
    val DEFAULT_FADE_IN_DURATION = FadeInDuration.THIRTY_SECONDS

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

    fun setOnBoardingShown(context: Context, value: Boolean) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putBoolean(SETTINGS_ON_BOARDING_SHOWN, value)
        editor.apply()
    }

    fun getOnBoardingShown(context: Context): Boolean {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getBoolean(
            SETTINGS_ON_BOARDING_SHOWN,
            DEFAULT_ON_BOARDING_SHOWN
        )
    }

    fun setLastOpenedTab(context: Context, tabFeature: TabFeature) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putInt(SETTINGS_LAST_OPENED_TAB, tabFeature.ordinal)
        editor.apply()
    }

    fun getLastOpenedTab(context: Context): TabFeature {
        val sharedPreferences = getSharedPreferences(context)
        return TabFeature.values()[sharedPreferences.getInt(
            SETTINGS_LAST_OPENED_TAB,
            DEFAULT_LAST_OPENED_TAB.ordinal
        )]
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

    fun setCanChangeSnoozeDuration(context: Context, value: Boolean) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putBoolean(SETTINGS_SNOOZE_CAN_CHANGE_DURATION, value)
        editor.apply()
    }

    fun getCanChangeSnoozeDuration(context: Context): Boolean {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getBoolean(
            SETTINGS_SNOOZE_CAN_CHANGE_DURATION,
            DEFAULT_SNOOZE_CAN_CHANGE_DURATION
        )
    }

    fun setDoubleTapSnooze(context: Context, value: Boolean) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putBoolean(SETTINGS_SNOOZE_DOUBLE_TAP, value)
        editor.apply()
    }

    fun isDoubleTapSnoozeEnabled(context: Context): Boolean {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getBoolean(SETTINGS_SNOOZE_DOUBLE_TAP, DEFAULT_SNOOZE_DOUBLE_TAP)
    }

    fun setSlideToTurnOff(context: Context, value: Boolean) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putBoolean(SETTINGS_SLIDE_TO_TURN_OFF, value)
        editor.apply()
    }

    fun getSlideToTurnOff(context: Context): Boolean {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getBoolean(SETTINGS_SLIDE_TO_TURN_OFF, DEFAULT_SLIDE_TO_TURN_OFF)
    }
}