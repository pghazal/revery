package com.pghaz.revery.viewmodel.timer

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.pghaz.revery.model.app.Timer
import com.pghaz.revery.repository.TimerRepository
import com.pghaz.revery.timer.TimerHandler
import com.pghaz.revery.viewmodel.BaseCreateEditViewModel

class CreateEditTimerViewModel(application: Application) : BaseCreateEditViewModel(application) {

    private val timerRepository = TimerRepository(application)

    val timerChangedLiveData = MutableLiveData<Timer>()

    fun createTimer(context: Context, timer: Timer) {
        TimerHandler.startTimer(timer)
        TimerHandler.setAlarm(context, timer)

        timerRepository.insert(timer)
    }

    fun editTimer(context: Context, timer: Timer) {
        TimerHandler.startTimer(timer)
        TimerHandler.setAlarm(context, timer)

        timerRepository.update(timer)
    }

    fun delete(timer: Timer) {
        timerRepository.delete(timer)
    }
}