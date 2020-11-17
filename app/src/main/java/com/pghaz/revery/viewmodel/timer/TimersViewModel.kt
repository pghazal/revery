package com.pghaz.revery.viewmodel.timer

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.pghaz.revery.broadcastreceiver.TimerBroadcastReceiver
import com.pghaz.revery.model.app.Timer
import com.pghaz.revery.model.app.TimerState
import com.pghaz.revery.repository.TimerRepository
import com.pghaz.revery.timer.TimerHandler
import com.pghaz.revery.util.Arguments

class TimersViewModel(application: Application) : AndroidViewModel(application) {

    private val timerRepository = TimerRepository(application)
    val timersLiveData = timerRepository.getTimersLiveData()

    fun startTimer(context: Context, timer: Timer) {
        TimerHandler.startTimer(timer)

        TimerHandler.setAlarm(context, timer)
    }

    fun stopTimer(context: Context, timer: Timer) {
        TimerHandler.resetTimer(timer)

        broadcastTimerShouldStop(context, timer, false)
    }

    fun pauseTimer(context: Context, timer: Timer) {
        TimerHandler.pauseTimer(timer)

        TimerHandler.removeAlarm(context, timer)
    }

    fun resetTimer(context: Context, timer: Timer) {
        TimerHandler.resetTimer(timer)

        TimerHandler.removeAlarm(context, timer)
    }

    fun incrementTimer(context: Context, timer: Timer) {
        if (timer.state == TimerState.RINGING) {
            broadcastTimerShouldStop(context, timer, true)
        }

        pauseTimer(context, timer)
        TimerHandler.incrementTimer(timer)
        startTimer(context, timer)
    }

    fun update(timer: Timer) {
        timerRepository.update(timer)
    }

    fun delete(timer: Timer) {
        timerRepository.delete(timer)
    }

    private fun broadcastTimerShouldStop(
        context: Context,
        timer: Timer,
        isIncrementAction: Boolean
    ) {
        val intent = TimerBroadcastReceiver.getStopTimerActionIntent(context, timer)
        intent.putExtra(Arguments.ARGS_TIMER_INCREMENT, isIncrementAction)
        context.sendBroadcast(intent)
    }
}