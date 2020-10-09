package com.pghaz.revery.alarm.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.pghaz.revery.alarm.repository.Alarm
import com.pghaz.revery.alarm.service.AlarmService
import com.pghaz.revery.alarm.service.RescheduleAlarmsService
import java.util.*

class AlarmBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) {
            return
        }

        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            val toastText = String.format("Alarm Reboot")
            Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()

            startRescheduleAlarmsService(context)
        } else {
            if (!intent.getBooleanExtra(Alarm.RECURRING, false)) {
                startAlarmService(context, intent)
            } else if (alarmIsToday(intent)) {
                startAlarmService(context, intent)
            }
        }
    }

    private fun alarmIsToday(intent: Intent): Boolean {
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()

        val today: Int = calendar.get(Calendar.DAY_OF_WEEK)

        when (today) {
            Calendar.MONDAY -> {
                return intent.getBooleanExtra(Alarm.MONDAY, false)
            }
            Calendar.TUESDAY -> {
                return intent.getBooleanExtra(Alarm.TUESDAY, false)
            }
            Calendar.WEDNESDAY -> {
                return intent.getBooleanExtra(Alarm.WEDNESDAY, false)
            }
            Calendar.THURSDAY -> {
                return intent.getBooleanExtra(Alarm.THURSDAY, false)
            }
            Calendar.FRIDAY -> {
                return intent.getBooleanExtra(Alarm.FRIDAY, false)
            }
            Calendar.SATURDAY -> {
                return intent.getBooleanExtra(Alarm.SATURDAY, false)
            }
            Calendar.SUNDAY -> {
                return intent.getBooleanExtra(Alarm.SUNDAY, false)
            }
        }

        return false
    }

    private fun startAlarmService(context: Context, intent: Intent) {
        val intentService = Intent(context, AlarmService::class.java)
        intentService.putExtra(Alarm.LABEL, intent.getStringExtra(Alarm.LABEL))
        intentService.putExtra(Alarm.RECURRING, intent.getBooleanExtra(Alarm.RECURRING, false))
        intentService.putExtra(Alarm.ID, intent.getLongExtra(Alarm.ID, 0))
        intentService.putExtra(Alarm.VIBRATE, intent.getBooleanExtra(Alarm.VIBRATE, false))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intentService)
        } else {
            context.startService(intentService)
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