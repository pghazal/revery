package com.pghaz.revery.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import com.pghaz.revery.BuildConfig
import com.pghaz.revery.R
import com.pghaz.revery.broadcastreceiver.AlarmBroadcastReceiver
import com.pghaz.revery.extension.toastDebug
import com.pghaz.revery.model.app.Alarm
import com.pghaz.revery.model.app.MediaMetadata
import com.pghaz.revery.model.app.MediaType
import com.pghaz.revery.ringtone.AudioPickerHelper
import com.pghaz.revery.settings.SettingsHandler
import com.pghaz.revery.util.DateTimeUtils
import java.util.*

object AlarmHandler {

    fun fireAlarmNow(
        context: Context,
        delayInSeconds: Int,
        metadata: MediaMetadata,
        fadeInDuration: Long
    ) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        // add delay in seconds
        calendar.add(Calendar.SECOND, delayInSeconds)

        val hour = DateTimeUtils.getCurrentHourOfDay(calendar)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        val alarm = Alarm(
            id = System.currentTimeMillis(),
            hour = hour,
            minute = minute,
            label = context.getString(R.string.alarm_test),
            enabled = true,
            recurring = false,
            monday = false,
            tuesday = false,
            wednesday = false,
            thursday = false,
            friday = false,
            saturday = false,
            sunday = false,
            vibrate = false,
            fadeIn = true,
            fadeInDuration = fadeInDuration,
            isSnooze = false,
            isPreview = true,
            metadata = metadata
        )

        scheduleAlarm(context, alarm, alarm.minute, second)
    }

    // This is for test purpose only
    fun fireAlarmNow(
        context: Context,
        delayInSeconds: Int,
        recurring: Boolean,
        spotify: Boolean,
        fadeIn: Boolean = false,
        fadeInDuration: Long = 0,
        vibrate: Boolean = false
    ) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        // add delay in seconds
        calendar.add(Calendar.SECOND, delayInSeconds)

        val hour = DateTimeUtils.getCurrentHourOfDay(calendar)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        val metadata = MediaMetadata()

        if (spotify) {
            metadata.type = MediaType.SPOTIFY_PLAYLIST
            metadata.uri = "spotify:playlist:3H8dsoJvkH7lUkaQlUNjPJ"
            metadata.shuffle = true
        } else {
            val uri = SettingsHandler.getAlarmDefaultAudioUri(context)

            val audioMetadata: AudioPickerHelper.AudioMetadata =
                AudioPickerHelper.getAudioMetadata(context, uri)

            metadata.type = MediaType.DEFAULT
            metadata.uri = uri.toString()
            metadata.name = audioMetadata.name
            metadata.description = audioMetadata.description
            metadata.imageUrl = audioMetadata.imageUrl
        }

        val alarm = Alarm(
            id = System.currentTimeMillis(),
            hour = hour,
            minute = minute,
            label = context.getString(R.string.alarm_test),
            enabled = true,
            recurring = recurring,
            monday = true,
            tuesday = true,
            wednesday = true,
            thursday = true,
            friday = true,
            saturday = true,
            sunday = true,
            vibrate = vibrate,
            fadeIn = fadeIn,
            fadeInDuration = fadeInDuration,
            isSnooze = false,
            isPreview = true,
            metadata = metadata
        )

        scheduleAlarm(context, alarm, alarm.minute, second)
    }

    fun scheduleAlarm(context: Context?, alarm: Alarm) {
        scheduleAlarm(context, alarm, alarm.minute, 0)
    }

    private fun scheduleAlarm(
        context: Context?,
        alarm: Alarm,
        minute: Int,
        second: Int
    ) {
        val alarmManager =
            context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

        alarmManager?.let {
            val intent = AlarmBroadcastReceiver.getScheduleAlarmActionIntent(context, alarm)

            val alarmPendingIntent =
                PendingIntent.getBroadcast(
                    context?.applicationContext,
                    alarm.id.toInt(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

            val now = System.currentTimeMillis()

            val calendar: Calendar = Calendar.getInstance()
            calendar.timeInMillis = now
            calendar.set(Calendar.HOUR_OF_DAY, alarm.hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, second)
            calendar.set(Calendar.MILLISECOND, 0)

            // if alarm time has already passed, increment day by 1
            if (calendar.timeInMillis <= now) {
                DateTimeUtils.incrementByOneDay(calendar)
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                alarmPendingIntent
            )

            alarm.enabled = true

            if (BuildConfig.DEBUG) {
                val toastText = String.format(
                    Locale.getDefault(),
                    "Alarm scheduled for %s at %02d:%02d with id %d. Recurring: %s",
                    DateTimeUtils.toDay(calendar[Calendar.DAY_OF_WEEK]),
                    alarm.hour,
                    alarm.minute,
                    alarm.id,
                    DateTimeUtils.getDaysText(alarm)
                )
                context?.toastDebug(toastText)
            }
        }
    }

    fun cancelAlarm(context: Context?, alarm: Alarm) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

        alarmManager?.let {
            val intent = AlarmBroadcastReceiver.getScheduleAlarmActionIntent(context, alarm)

            val alarmPendingIntent =
                PendingIntent.getBroadcast(
                    context?.applicationContext,
                    alarm.id.toInt(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            it.cancel(alarmPendingIntent)

            alarm.enabled = false

            if (BuildConfig.DEBUG) {
                val toastText = String.format(
                    Locale.getDefault(),
                    "Alarm cancel at %02d:%02d with id %d.",
                    alarm.hour,
                    alarm.minute,
                    alarm.id
                )
                context?.toastDebug(toastText)
            }
        }
    }

    fun snooze(context: Context?, alarm: Alarm, delayInMinutes: Int): Alarm {
        val now = System.currentTimeMillis()

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = now
        // add delay in minutes
        calendar.add(Calendar.MINUTE, delayInMinutes)

        val hour = DateTimeUtils.getCurrentHourOfDay(calendar)
        val minute = calendar.get(Calendar.MINUTE)

        val snoozeAlarm = Alarm(alarm)

        snoozeAlarm.id = now
        snoozeAlarm.recurring = false
        snoozeAlarm.enabled = true
        snoozeAlarm.hour = hour
        snoozeAlarm.minute = minute
        snoozeAlarm.isSnooze = true

        scheduleAlarm(context, snoozeAlarm)

        return snoozeAlarm
    }
}