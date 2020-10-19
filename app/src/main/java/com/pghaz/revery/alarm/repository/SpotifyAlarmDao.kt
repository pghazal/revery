package com.pghaz.revery.alarm.repository

import androidx.room.Dao
import androidx.room.RoomDatabase
import com.pghaz.revery.alarm.model.room.RSpotifyAlarm

@Dao
abstract class SpotifyAlarmDao(roomDatabase: RoomDatabase) :
    AbstractDao<RSpotifyAlarm>(AlarmDatabase.ALARM_SPOTIFY_TABLE_NAME, roomDatabase)