package com.pghaz.revery.viewmodel.timer

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.pghaz.revery.broadcastreceiver.TimerBroadcastReceiver
import com.pghaz.revery.model.app.Timer
import com.pghaz.revery.model.app.TimerState
import com.pghaz.revery.repository.TimerRepository
import com.pghaz.revery.timer.TimerHandler

class TimersViewModel(application: Application) : AndroidViewModel(application) {

    private val timerRepository = TimerRepository(application)
    val timersLiveData = timerRepository.getTimersLiveData()

    fun startTimer(context: Context, timer: Timer) {
        TimerHandler.startTimer(timer)

        TimerHandler.setAlarm(context, timer)
    }

    fun stopTimer(context: Context, timer: Timer) {
        TimerHandler.resetTimer(timer)

        val intent = TimerBroadcastReceiver.buildStopRingingTimerActionIntent(context, timer)
        context.sendBroadcast(intent)
    }

    fun pauseTimer(context: Context, timer: Timer) {
        TimerHandler.pauseTimer(timer)

        TimerHandler.removeAlarm(context, timer)
    }

    fun resetTimer(context: Context, timer: Timer) {
        TimerHandler.resetTimer(timer)

        TimerHandler.removeAlarm(context, timer)
    }

    fun incrementTimer(context: Context, timer: Timer, incrementValue: Int) {
        if (timer.state == TimerState.RINGING) {
            val incrementIntent =
                TimerBroadcastReceiver.buildRingingTimerIncrementActionIntent(context, timer, incrementValue)
            context.sendBroadcast(incrementIntent)

            val stopIntent =
                TimerBroadcastReceiver.buildStopRingingTimerActionIntent(context, timer)
            context.sendBroadcast(stopIntent)
        } else {
            val incrementIntent =
                TimerBroadcastReceiver.buildRunningTimerIncrementActionIntent(context, timer, incrementValue)
            context.sendBroadcast(incrementIntent)
        }
    }

    fun update(timer: Timer) {
        timerRepository.update(timer)
    }

    fun delete(timer: Timer) {
        timerRepository.delete(timer)
    }
}