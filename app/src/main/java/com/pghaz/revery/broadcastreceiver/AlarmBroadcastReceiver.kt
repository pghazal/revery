package com.pghaz.revery.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.pghaz.revery.alarm.AlarmHandler
import com.pghaz.revery.alarm.RingActivity
import com.pghaz.revery.model.app.alarm.Alarm
import com.pghaz.revery.service.AlarmService
import com.pghaz.revery.service.RescheduleAlarmsService
import com.pghaz.revery.settings.SettingsHandler
import com.pghaz.revery.util.IntentUtils
import java.util.*

class AlarmBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_ALARM_FIRES = "com.pghaz.revery.ACTION_ALARM_FIRES"
        const val ACTION_ALARM_STOP = "com.pghaz.revery.ACTION_ALARM_STOP"
        const val ACTION_ALARM_SNOOZE = "com.pghaz.revery.ACTION_ALARM_SNOOZE"

        fun getScheduleAlarmActionIntent(context: Context?, alarm: Alarm): Intent {
            val intent = Intent(context?.applicationContext, AlarmBroadcastReceiver::class.java)
            intent.action = ACTION_ALARM_FIRES
            // This is a workaround due to problems with Parcelables into Intent
            // See: https://stackoverflow.com/questions/39478422/pendingintent-getbroadcast-lost-parcelable-data

            IntentUtils.safePutAlarmIntoIntent(intent, alarm)

            return intent
        }

        fun getStopAlarmActionIntent(context: Context?, alarm: Alarm): Intent {
            val intent = Intent(context?.applicationContext, AlarmBroadcastReceiver::class.java)
            intent.action = ACTION_ALARM_STOP

            IntentUtils.safePutAlarmIntoIntent(intent, alarm)

            return intent
        }

        fun getSnoozeActionIntent(context: Context?, alarm: Alarm): Intent {
            val intent = Intent(context?.applicationContext, AlarmBroadcastReceiver::class.java)
            intent.action = ACTION_ALARM_SNOOZE

            IntentUtils.safePutAlarmIntoIntent(intent, alarm)

            return intent
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) {
            return
        }

        if (Intent.ACTION_BOOT_COMPLETED == intent.action ||
            "android.intent.action.QUICKBOOT_POWERON" == intent.action ||
            "com.htc.intent.action.QUICKBOOT_POWERON" == intent.action ||
            Intent.ACTION_PACKAGE_REPLACED == intent.action ||
            Intent.ACTION_TIME_CHANGED == intent.action ||
            Intent.ACTION_TIMEZONE_CHANGED == intent.action ||
            Intent.ACTION_DATE_CHANGED == intent.action
        ) {
            startRescheduleAlarmsService(context)
        } else if (ACTION_ALARM_FIRES == intent.action) {
            val alarm = IntentUtils.safeGetAlarmFromIntent(intent)

            if (!alarm.recurring) {
                startAlarmService(context, alarm)
            } else if (alarm.recurring && alarmIsToday(alarm)) {
                startAlarmService(context, alarm)

                // Reschedule next alarm to same time the day after
                AlarmHandler.scheduleAlarm(context, alarm)
            }

        } else if (ACTION_ALARM_SNOOZE == intent.action) {
            val alarm = IntentUtils.safeGetAlarmFromIntent(intent)

            // TODO show a notification when snoozed ?
            val snoozeMinutes = SettingsHandler.getSnoozeDuration(context)
            AlarmHandler.snooze(context, alarm, snoozeMinutes)

            broadcastFinishRingActivity(context)
            broadcastServiceSnooze(context)
        } else if (ACTION_ALARM_STOP == intent.action) {
            broadcastFinishRingActivity(context)
            broadcastServiceShouldStop(context)
        }
    }

    private fun broadcastFinishRingActivity(context: Context) {
        val stopRingActivityIntent = RingActivity.getFinishRingActivityBroadcastReceiver(context)
        LocalBroadcastManager.getInstance(context).sendBroadcast(stopRingActivityIntent)
    }

    private fun broadcastServiceShouldStop(context: Context) {
        val serviceShouldStopIntent = AlarmService.getServiceShouldStopIntent(context)
        LocalBroadcastManager.getInstance(context).sendBroadcast(serviceShouldStopIntent)
    }

    private fun broadcastServiceSnooze(context: Context) {
        val serviceShouldStopIntent = AlarmService.getServiceSnoozeIntent(context)
        LocalBroadcastManager.getInstance(context).sendBroadcast(serviceShouldStopIntent)
    }

    private fun alarmIsToday(alarm: Alarm): Boolean {
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()

        val today: Int = calendar.get(Calendar.DAY_OF_WEEK)

        when (today) {
            Calendar.MONDAY -> {
                return alarm.monday
            }
            Calendar.TUESDAY -> {
                return alarm.tuesday
            }
            Calendar.WEDNESDAY -> {
                return alarm.wednesday
            }
            Calendar.THURSDAY -> {
                return alarm.thursday
            }
            Calendar.FRIDAY -> {
                return alarm.friday
            }
            Calendar.SATURDAY -> {
                return alarm.saturday
            }
            Calendar.SUNDAY -> {
                return alarm.sunday
            }
        }

        return false
    }

    private fun startAlarmService(context: Context, alarm: Alarm) {
        val service = Intent(context, AlarmService::class.java)

        IntentUtils.safePutAlarmIntoIntent(service, alarm)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(service)
        } else {
            context.startService(service)
        }
    }

    private fun startRescheduleAlarmsService(context: Context) {
        val service = Intent(context, RescheduleAlarmsService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(service)
        } else {
            context.startService(service)
        }
    }
}