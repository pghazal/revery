package com.pghaz.revery.viewmodel.timer

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.pghaz.revery.model.app.Timer
import com.pghaz.revery.repository.TimerRepository

class TimersViewModel(application: Application) : AndroidViewModel(application) {

    private val timerRepository = TimerRepository(application)
    val timersLiveData = timerRepository.getTimersLiveData()

    fun scheduleTimer(context: Context?, timer: Timer) {
        //AlarmHandler.scheduleAlarm(context, timer)
    }

    fun cancelTimer(context: Context?, timer: Timer) {
        //AlarmHandler.cancelAlarm(context, timer)
    }

    fun update(timer: Timer) {
        timerRepository.update(timer)
    }

    fun delete(timer: Timer) {
        timerRepository.delete(timer)
    }
}