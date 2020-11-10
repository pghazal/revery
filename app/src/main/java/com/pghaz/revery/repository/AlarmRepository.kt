package com.pghaz.revery.repository

import android.app.Application
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.pghaz.revery.BuildConfig
import com.pghaz.revery.model.app.Alarm
import com.pghaz.revery.model.room.RAlarm
import com.pghaz.revery.model.room.RBaseModel
import kotlinx.coroutines.launch

class AlarmRepository(application: Application) {

    private val alarmDao: AlarmDao = ReveryDatabase.getDatabase(application).alarmDao()
    private val alarmsLiveData: LiveData<List<RAlarm>> = alarmDao.getEntities()

    @MainThread
    fun insert(alarm: Alarm) {
        ReveryDatabase.databaseCoroutinesScope.launch {
            alarmDao.insert(Alarm.toDatabaseModel(alarm))
        }
    }

    @MainThread
    fun update(alarm: Alarm) {
        ReveryDatabase.databaseCoroutinesScope.launch {
            alarmDao.update(Alarm.toDatabaseModel(alarm))
        }
    }

    @MainThread
    fun get(alarm: Alarm): LiveData<Alarm> {
        return Transformations.map(alarmDao.getEntity(alarm.id)) {
            // When we fire alarm that are not into DB, we just need an non null alarm
            if (BuildConfig.DEBUG) {
                if (it == null) {
                    return@map Alarm.fromDatabaseModel(RAlarm(id = RBaseModel.NO_ID))
                }
            }

            return@map Alarm.fromDatabaseModel(it)
        }
    }

    @MainThread
    fun delete(alarm: Alarm) {
        ReveryDatabase.databaseCoroutinesScope.launch {
            alarmDao.delete(Alarm.toDatabaseModel(alarm))
        }
    }

    @MainThread
    fun deleteAll() {
        ReveryDatabase.databaseCoroutinesScope.launch {
            alarmDao.deleteAll()
        }
    }

    @MainThread
    fun getAlarmsLiveData(): LiveData<List<Alarm>> {
        return Transformations.map(alarmsLiveData) { alarmsFromDb ->
            val alarms = ArrayList<Alarm>()

            alarmsFromDb.forEach { alarmFromDb ->
                alarms.add(Alarm.fromDatabaseModel(alarmFromDb))
            }

            return@map alarms
        }
    }
}