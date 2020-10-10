package com.pghaz.revery.alarm.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.pghaz.revery.alarm.model.app.Alarm
import com.pghaz.revery.alarm.model.app.AlarmMetadata
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
        val service = Intent(context, AlarmService::class.java)
        service.putExtra(Alarm.LABEL, intent.getStringExtra(Alarm.LABEL))
        service.putExtra(Alarm.RECURRING, intent.getBooleanExtra(Alarm.RECURRING, false))
        service.putExtra(Alarm.ID, intent.getLongExtra(Alarm.ID, 0))
        service.putExtra(Alarm.VIBRATE, intent.getBooleanExtra(Alarm.VIBRATE, false))

        val metadataBundle = intent.getBundleExtra(Alarm.METADATA)
        metadataBundle?.let {
            val metadata = it.getParcelable(Alarm.METADATA) as AlarmMetadata?
            service.putExtra(Alarm.METADATA, metadata)
        }

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