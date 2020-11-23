package com.pghaz.revery.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.pghaz.revery.model.app.StandByEnabler
import com.pghaz.revery.service.StandByService
import com.pghaz.revery.util.IntentUtils

class StandByBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_STANDBY_START = "com.pghaz.revery.ACTION_STANDBY_START"

        fun buildStandByStartActionIntent(
            context: Context?,
            standByEnabler: StandByEnabler
        ): Intent {
            val intent = Intent(context?.applicationContext, StandByBroadcastReceiver::class.java)
            intent.action = ACTION_STANDBY_START

            IntentUtils.safePutStandByEnablerIntoIntent(intent, standByEnabler)

            return intent
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) {
            return
        }

        if (ACTION_STANDBY_START == intent.action) {
            val standByEnabler = IntentUtils.safeGetStandByEnablerFromIntent(intent)
            broadcastStandByStartService(context, standByEnabler)
        }
    }

    private fun broadcastStandByStartService(context: Context, standByEnabler: StandByEnabler) {
        val service = Intent(context, StandByService::class.java)

        IntentUtils.safePutStandByEnablerIntoIntent(service, standByEnabler)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(service)
        } else {
            context.startService(service)
        }
    }
}