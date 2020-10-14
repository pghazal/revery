package com.pghaz.revery.alarm.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.pghaz.revery.alarm.AlarmHandler
import com.pghaz.revery.alarm.RingActivity
import com.pghaz.revery.alarm.model.app.Alarm
import com.pghaz.revery.alarm.service.AlarmService
import com.pghaz.revery.alarm.service.RescheduleAlarmsService
import com.pghaz.revery.settings.SettingsHandler
import com.pghaz.revery.util.IntentUtils
import java.util.*

class AlarmBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmBroadcastReceiver"

        const val ACTION_ALARM_SCHEDULE = "com.pghaz.revery.ACTION_ALARM_SCHEDULE"
        const val ACTION_ALARM_STOP = "com.pghaz.revery.ACTION_ALARM_STOP"
        const val ACTION_ALARM_SNOOZE = "com.pghaz.revery.ACTION_ALARM_SNOOZE"

        fun getScheduleAlarmActionIntent(context: Context?, alarm: Alarm): Intent {
            val intent = Intent(context?.applicationContext, AlarmBroadcastReceiver::class.java)
            intent.action = ACTION_ALARM_SCHEDULE
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

        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            val toastText = String.format("Alarm Reboot")
            Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()

            startRescheduleAlarmsService(context)

        } else if (ACTION_ALARM_SCHEDULE == intent.action) {
            val alarm = IntentUtils.safeGetAlarmFromIntent(intent)

            if (!alarm.recurring) {
                startAlarmService(context, alarm)
            } else if (alarmIsToday(alarm)) {
                startAlarmService(context, alarm)
            }

        } else if (ACTION_ALARM_SNOOZE == intent.action) {
            val alarm = IntentUtils.safeGetAlarmFromIntent(intent)

            // TODO show a notification when snoozed ?
            val snoozeMinutes = SettingsHandler.getSnoozeDuration(context)
            AlarmHandler.snooze(context, alarm, snoozeMinutes)

            stopService(context)
        } else if (ACTION_ALARM_STOP == intent.action) {
            stopService(context)
        }
    }

    private fun stopService(context: Context) {
        broadcastAlarmStopped(context)

        val service = Intent(context.applicationContext, AlarmService::class.java)
        context.applicationContext.stopService(service)
    }

    private fun broadcastAlarmStopped(context: Context) {
        val stopRingActivityIntent = RingActivity.getAlarmStoppedBroadcastReceiver(context)
        LocalBroadcastManager.getInstance(context).sendBroadcast(stopRingActivityIntent)
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
        val intentService = Intent(context, RescheduleAlarmsService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intentService)
        } else {
            context.startService(intentService)
        }
    }
}