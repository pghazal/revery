package com.pghaz.revery.alarm.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import com.pghaz.revery.alarm.model.app.Alarm
import com.pghaz.revery.alarm.service.AlarmService
import com.pghaz.revery.alarm.service.RescheduleAlarmsService
import com.pghaz.revery.util.Arguments
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
            val alarmBundle = intent.getBundleExtra(Arguments.ARGS_BUNDLE_ALARM)
            val alarm = alarmBundle?.getParcelable<Alarm>(Arguments.ARGS_ALARM) as Alarm

            if (!alarm.recurring) {
                startAlarmService(context, alarm)
            } else if (alarmIsToday(alarm)) {
                startAlarmService(context, alarm)
            }
        }
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

        val alarmBundle = Bundle()
        alarmBundle.putParcelable(Arguments.ARGS_ALARM, alarm)
        service.putExtra(Arguments.ARGS_BUNDLE_ALARM, alarmBundle)

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