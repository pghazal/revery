package com.pghaz.revery.alarm.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.pghaz.revery.BuildConfig
import com.pghaz.revery.alarm.model.app.Alarm
import com.pghaz.revery.alarm.model.room.RAlarm
import kotlinx.coroutines.launch

class AlarmRepository(application: Application) {

    private val alarmDao: AlarmDao = AlarmDatabase.getDatabase(application).alarmDao()

    private val alarmsLiveData: LiveData<List<RAlarm>> = alarmDao.getEntities()

    fun insert(alarm: Alarm) {
        AlarmDatabase.databaseCoroutinesScope.launch {
            alarmDao.insert(Alarm.toDatabaseModel(alarm))
        }
    }

    fun update(alarm: Alarm) {
        AlarmDatabase.databaseCoroutinesScope.launch {
            alarmDao.update(Alarm.toDatabaseModel(alarm))
        }
    }

    fun get(alarm: Alarm): LiveData<Alarm> {
        return Transformations.map(alarmDao.getEntity(alarm.id)) {
            // When we fire alarm that are not into DB, we just need an non null alarm
            if (BuildConfig.DEBUG) {
                if (it == null) {
                    return@map Alarm.fromDatabaseModel(RAlarm(id = 0))
                }
            }

            return@map Alarm.fromDatabaseModel(it)
        }
    }

    fun delete(alarm: Alarm) {
        AlarmDatabase.databaseCoroutinesScope.launch {
            alarmDao.delete(Alarm.toDatabaseModel(alarm))
        }
    }

    fun deleteAll() {
        AlarmDatabase.databaseCoroutinesScope.launch {
            alarmDao.deleteAll()
        }
    }

    /*fun getAllAlarmsLiveData(): LiveData<List<Alarm>> {
        val mediatorLiveData = MediatorLiveData<List<RAlarm>>()

        mediatorLiveData.addSource(alarmsLiveData) {
            val alarms = ArrayList<RAlarm>()
            alarms.addAll(it)

            if (spotifyAlarmsLiveData.value != null) {
                alarms.addAll(spotifyAlarmsLiveData.value!!)
            }

            mediatorLiveData.postValue(alarms)
        }

        mediatorLiveData.addSource(spotifyAlarmsLiveData) {
            val alarms = ArrayList<RAlarm>()
            alarms.addAll(it)

            if (alarmsLiveData.value != null) {
                alarms.addAll(alarmsLiveData.value!!)
            }

            mediatorLiveData.postValue(alarms)
        }

        return Transformations.map(mediatorLiveData) { alarmsFromDb ->
            val result = ArrayList<Alarm>()

            alarmsFromDb.forEach { abstractAlarm ->
                when (abstractAlarm) {
                    is RAlarm -> result.add(Alarm.fromDatabaseModel(abstractAlarm))
                    is RSpotifyAlarm -> result.add(SpotifyAlarm.fromDatabaseModel(abstractAlarm))
                }
            }

            result.sortBy { it.id }

            return@map result
        }
    }*/

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