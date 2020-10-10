package com.pghaz.revery.alarm.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.pghaz.revery.alarm.model.app.Alarm
import com.pghaz.revery.alarm.model.room.RAlarm

class AlarmRepository(application: Application) {

    private val alarmDao: AlarmDao = AlarmDatabase.getDatabase(application).alarmDao()
    private val alarmsLiveData: LiveData<List<RAlarm>> = alarmDao.getAlarms()

    fun insert(alarm: Alarm) {
        AlarmDatabase.databaseWriteExecutor.execute {
            alarm.id = alarmDao.insert(Alarm.toDatabaseModel(alarm))
        }
    }

    fun update(alarm: Alarm) {
        AlarmDatabase.databaseWriteExecutor.execute {
            alarmDao.update(Alarm.toDatabaseModel(alarm))
        }
    }

    fun get(id: Long): LiveData<Alarm> {
        return Transformations.map(alarmDao.get(id)) {
            return@map Alarm.fromDatabaseModel(it)
        }
    }

    fun delete(alarm: Alarm) {
        AlarmDatabase.databaseWriteExecutor.execute {
            alarmDao.delete(Alarm.toDatabaseModel(alarm))
        }
    }

    fun deleteAll() {
        AlarmDatabase.databaseWriteExecutor.execute {
            alarmDao.deleteAll()
        }
    }

    fun getAlarmsLiveData(): LiveData<List<Alarm>> {
        return Transformations.map(alarmsLiveData) { alarmsFromDb ->
            val alarms = ArrayList<Alarm>()

            alarmsFromDb.forEach { alarm ->
                alarms.add(Alarm.fromDatabaseModel(alarm))
            }

            return@map alarms
        }
    }
}