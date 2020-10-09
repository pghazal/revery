package com.pghaz.revery.alarm.repository

import android.app.Application
import androidx.lifecycle.LiveData


class AlarmRepository(application: Application) {

    private val alarmDao: AlarmDao = AlarmDatabase.getDatabase(application).alarmDao()
    private var alarmsLiveData: LiveData<List<Alarm>> = alarmDao.getAlarms()

    fun insert(alarm: Alarm) {
        AlarmDatabase.databaseWriteExecutor.execute {
            alarm.id = alarmDao.insert(alarm)
        }
    }

    fun update(alarm: Alarm) {
        AlarmDatabase.databaseWriteExecutor.execute {
            alarmDao.update(alarm)
        }
    }

    fun get(id: Long): LiveData<Alarm> {
        return alarmDao.get(id)
    }

    fun delete(alarm: Alarm) {
        AlarmDatabase.databaseWriteExecutor.execute {
            alarmDao.delete(alarm)
        }
    }

    fun deleteAll() {
        AlarmDatabase.databaseWriteExecutor.execute {
            alarmDao.deleteAll()
        }
    }

    fun getAlarmsLiveData(): LiveData<List<Alarm>> {
        return alarmsLiveData
    }
}