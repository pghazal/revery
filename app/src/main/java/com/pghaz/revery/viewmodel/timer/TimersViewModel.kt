package com.pghaz.revery.viewmodel.timer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.pghaz.revery.model.app.Timer
import com.pghaz.revery.repository.TimerRepository
import com.pghaz.revery.timer.TimerHandler

class TimersViewModel(application: Application) : AndroidViewModel(application) {

    private val timerRepository = TimerRepository(application)
    val timersLiveData = timerRepository.getTimersLiveData()

    fun startTimer(timer: Timer) {
        TimerHandler.startTimer(timer)
    }

    fun pauseTimer(timer: Timer) {
        TimerHandler.pauseTimer(timer)
    }

    fun resetTimer(timer: Timer) {
        TimerHandler.resetTimer(timer)
    }

    fun update(timer: Timer) {
        timerRepository.update(timer)
    }

    fun delete(timer: Timer) {
        timerRepository.delete(timer)
    }
}