package com.pghaz.revery.repository

import android.app.Application
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.pghaz.revery.BuildConfig
import com.pghaz.revery.model.app.Timer
import com.pghaz.revery.model.room.RBaseModel
import com.pghaz.revery.model.room.RTimer
import kotlinx.coroutines.launch

class TimerRepository(application: Application) {

    private val timerDao: TimerDao = ReveryDatabase.getDatabase(application).timerDao()
    private val timersLiveData: LiveData<List<RTimer>> = timerDao.getEntities()

    @MainThread
    fun insert(timer: Timer) {
        ReveryDatabase.databaseCoroutinesScope.launch {
            timerDao.insert(Timer.toDatabaseModel(timer))
        }
    }

    @MainThread
    fun update(timer: Timer) {
        ReveryDatabase.databaseCoroutinesScope.launch {
            timerDao.update(Timer.toDatabaseModel(timer))
        }
    }

    @MainThread
    fun get(timer: Timer): LiveData<Timer> {
        return Transformations.map(timerDao.getEntity(timer.id)) {
            // When we fire timer that are not into DB, we just need an non null timer
            if (BuildConfig.DEBUG) {
                if (it == null) {
                    return@map Timer.fromDatabaseModel(RTimer(id = RBaseModel.NO_ID))
                }
            }

            return@map Timer.fromDatabaseModel(it)
        }
    }

    @MainThread
    fun delete(timer: Timer) {
        ReveryDatabase.databaseCoroutinesScope.launch {
            timerDao.delete(Timer.toDatabaseModel(timer))
        }
    }

    @MainThread
    fun deleteAll() {
        ReveryDatabase.databaseCoroutinesScope.launch {
            timerDao.deleteAll()
        }
    }

    @MainThread
    fun getTimersLiveData(): LiveData<List<Timer>> {
        return Transformations.map(timersLiveData) { timersFromDb ->
            val timers = ArrayList<Timer>()

            timersFromDb.forEach { timerFromDb ->
                timers.add(Timer.fromDatabaseModel(timerFromDb))
            }

            return@map timers
        }
    }
}