package com.pghaz.revery.alarm.repository

import androidx.room.Dao
import androidx.room.RoomDatabase
import com.pghaz.revery.alarm.model.room.RAlarm

@Dao
abstract class AlarmDao(roomDatabase: RoomDatabase) :
    AbstractDao<RAlarm>(AlarmDatabase.ALARM_DEFAULT_TABLE_NAME, roomDatabase)