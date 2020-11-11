package com.pghaz.revery.viewmodel.timer

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.pghaz.revery.model.app.Timer
import com.pghaz.revery.repository.TimerRepository
import com.pghaz.revery.viewmodel.BaseCreateEditViewModel

class CreateEditTimerViewModel(application: Application) : BaseCreateEditViewModel(application) {

    private val timerRepository = TimerRepository(application)

    val timerChangedLiveData = MutableLiveData<Timer>()

    fun createTimer(context: Context?, timer: Timer) {
        timerRepository.insert(timer)
        //AlarmHandler.scheduleAlarm(context, timer)
    }

    fun editTimer(context: Context?, timer: Timer) {
        //AlarmHandler.cancelAlarm(context, timer)
        //AlarmHandler.scheduleAlarm(context, timer)

        timerRepository.update(timer)
    }

    fun delete(context: Context?, timer: Timer) {
        //AlarmHandler.cancelAlarm(context, timer)
        timerRepository.delete(timer)
    }
}