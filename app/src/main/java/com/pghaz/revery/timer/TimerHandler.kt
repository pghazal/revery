package com.pghaz.revery.timer

import com.pghaz.revery.model.app.Timer
import com.pghaz.revery.model.app.TimerState

object TimerHandler {

    fun startTimer(timer: Timer) {
        val now = System.currentTimeMillis()
        timer.startTime = now

        if (timer.stopTime == 0L) {
            timer.stopTime = now + timer.duration
        } else {
            timer.stopTime = now + timer.remainingTime
        }

        timer.state = TimerState.RUNNING
    }

    fun pauseTimer(timer: Timer) {
        timer.remainingTime = timer.stopTime - System.currentTimeMillis()
        timer.state = TimerState.PAUSED
    }

    fun resetTimer(timer: Timer) {
        timer.startTime = 0
        timer.stopTime = 0
        timer.remainingTime = 0
        timer.state = TimerState.CREATED
    }

    fun incrementTimer(timer: Timer) {
        timer.remainingTime += 60 * 1000
    }

    fun getElapsedTime(timer: Timer): Long {
        val now = System.currentTimeMillis()
        val elapsedTime: Long

        if (timer.state == TimerState.RUNNING && now >= timer.stopTime) {
            elapsedTime = 0
            resetTimer(timer)
        } else if (timer.state != TimerState.RUNNING && timer.stopTime != 0L) {
            elapsedTime = timer.remainingTime
        } else if (timer.state != TimerState.RUNNING && timer.stopTime == 0L) {
            elapsedTime = 0
        } else {
            elapsedTime = timer.stopTime - now
        }

        return elapsedTime
    }
}