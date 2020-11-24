package com.pghaz.revery.standby

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import com.pghaz.revery.BuildConfig
import com.pghaz.revery.broadcastreceiver.StandByBroadcastReceiver
import com.pghaz.revery.extension.toastDebug
import com.pghaz.revery.model.app.StandByEnabler
import com.pghaz.revery.util.DateTimeUtils
import java.util.*

object StandByHandler {

    private const val PENDING_INTENT_STANDBY_ID = 2121

    fun setAlarm(
        context: Context?,
        is24HourFormat: Boolean,
        standByEnabler: StandByEnabler
    ) {
        if (context == null) {
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

        alarmManager?.let {
            val intent =
                StandByBroadcastReceiver.buildStandByStartActionIntent(context, standByEnabler)

            val now = System.currentTimeMillis()
            val calendar = Calendar.getInstance()
            calendar.set(
                if (is24HourFormat) Calendar.HOUR_OF_DAY else Calendar.HOUR,
                standByEnabler.hour
            )
            calendar.set(Calendar.MINUTE, standByEnabler.minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            // if alarm time has already passed, increment day by 1
            if (calendar.timeInMillis <= now) {
                DateTimeUtils.incrementByOneDay(calendar)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context.applicationContext,
                PENDING_INTENT_STANDBY_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            it.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )

            if (BuildConfig.DEBUG) {
                val toastText = String.format(
                    Locale.getDefault(),
                    "Standby scheduled for %s at %02d:%02d. Fade out: %d",
                    DateTimeUtils.toDay(calendar[Calendar.DAY_OF_WEEK]),
                    standByEnabler.hour,
                    standByEnabler.minute,
                    standByEnabler.fadeOutDuration
                )
                context.toastDebug(toastText)
            }
        }
    }

    fun removeAlarm(context: Context?, standByEnabler: StandByEnabler) {
        if (context == null) {
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

        alarmManager?.let {
            val intent =
                StandByBroadcastReceiver.buildStandByStartActionIntent(context, standByEnabler)

            val pendingIntent = PendingIntent.getBroadcast(
                context.applicationContext,
                PENDING_INTENT_STANDBY_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            it.cancel(pendingIntent)
        }
    }
}