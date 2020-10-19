package com.pghaz.revery.alarm.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import com.pghaz.revery.BuildConfig
import com.pghaz.revery.alarm.model.app.AbstractAlarm
import com.pghaz.revery.alarm.model.app.Alarm
import com.pghaz.revery.alarm.model.app.SpotifyAlarm
import com.pghaz.revery.alarm.model.room.RAbstractAlarm
import com.pghaz.revery.alarm.model.room.RAlarm
import com.pghaz.revery.alarm.model.room.RSpotifyAlarm
import kotlinx.coroutines.launch

class AlarmRepository(application: Application) {

    private val alarmDao: AlarmDao = AlarmDatabase.getDatabase(application).alarmDao()
    private val spotifyAlarmDao: SpotifyAlarmDao =
        AlarmDatabase.getDatabase(application).spotifyAlarmDao()

    private val alarmsLiveData: LiveData<List<RAlarm>> = alarmDao.getEntities()
    private val spotifyAlarmsLiveData: LiveData<List<RSpotifyAlarm>> = spotifyAlarmDao.getEntities()

    fun insert(alarm: AbstractAlarm) {
        AlarmDatabase.databaseCoroutinesScope.launch {
            // This setter ´alarm.id =´ is not even used since we use ´System.currentTimeInMillis()´ as id
            alarm.id = when (alarm) {
                is SpotifyAlarm -> spotifyAlarmDao.insert(SpotifyAlarm.toDatabaseModel(alarm))
                is Alarm -> alarmDao.insert(Alarm.toDatabaseModel(alarm))
                else -> throw IllegalArgumentException("AlarmRepository insert: alarm unknown type")
            }
        }
    }

    fun update(alarm: AbstractAlarm) {
        AlarmDatabase.databaseCoroutinesScope.launch {
            when (alarm) {
                is SpotifyAlarm -> spotifyAlarmDao.update(SpotifyAlarm.toDatabaseModel(alarm))
                is Alarm -> alarmDao.update(Alarm.toDatabaseModel(alarm))
                else -> throw IllegalArgumentException("AlarmRepository update: alarm unknown type")
            }
        }
    }

    fun get(alarm: AbstractAlarm): LiveData<AbstractAlarm> {
        when (alarm) {
            is Alarm -> {
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

            is SpotifyAlarm -> {
                return Transformations.map(spotifyAlarmDao.getEntity(alarm.id)) {
                    // When we fire alarm that are not into DB, we just need an non null alarm
                    if (BuildConfig.DEBUG) {
                        if (it == null) {
                            return@map SpotifyAlarm.fromDatabaseModel(RSpotifyAlarm(id = 0))
                        }
                    }


                    return@map SpotifyAlarm.fromDatabaseModel(it)
                }
            }

            else -> throw IllegalArgumentException("AlarmRepository get: alarm unknown type")
        }
    }

    fun delete(alarm: AbstractAlarm) {
        AlarmDatabase.databaseCoroutinesScope.launch {
            when (alarm) {
                is SpotifyAlarm -> spotifyAlarmDao.delete(SpotifyAlarm.toDatabaseModel(alarm))
                is Alarm -> alarmDao.delete(Alarm.toDatabaseModel(alarm))
                else -> throw IllegalArgumentException("AlarmRepository delete: alarm unknown type")
            }
        }
    }

    fun deleteAll() {
        AlarmDatabase.databaseCoroutinesScope.launch {
            alarmDao.deleteAll()
            spotifyAlarmDao.deleteAll()
        }
    }

    fun getAllAlarmsLiveData(): LiveData<List<AbstractAlarm>> {
        val mediatorLiveData = MediatorLiveData<List<RAbstractAlarm>>()

        mediatorLiveData.addSource(alarmsLiveData) {
            val alarms = ArrayList<RAbstractAlarm>()
            alarms.addAll(it)

            if (spotifyAlarmsLiveData.value != null) {
                alarms.addAll(spotifyAlarmsLiveData.value!!)
            }

            mediatorLiveData.postValue(alarms)
        }

        mediatorLiveData.addSource(spotifyAlarmsLiveData) {
            val alarms = ArrayList<RAbstractAlarm>()
            alarms.addAll(it)

            if (alarmsLiveData.value != null) {
                alarms.addAll(alarmsLiveData.value!!)
            }

            mediatorLiveData.postValue(alarms)
        }

        return Transformations.map(mediatorLiveData) { alarmsFromDb ->
            val result = ArrayList<AbstractAlarm>()

            alarmsFromDb.forEach { abstractAlarm ->
                when (abstractAlarm) {
                    is RAlarm -> result.add(Alarm.fromDatabaseModel(abstractAlarm))
                    is RSpotifyAlarm -> result.add(SpotifyAlarm.fromDatabaseModel(abstractAlarm))
                }
            }

            result.sortBy { it.id }

            return@map result
        }
    }

    fun getAlarmsLiveData(): LiveData<List<AbstractAlarm>> {
        return Transformations.map(alarmsLiveData) { alarmsFromDb ->
            val alarms = ArrayList<Alarm>()

            alarmsFromDb.forEach { alarm ->
                alarms.add(Alarm.fromDatabaseModel(alarm))
            }

            return@map alarms
        }
    }
}