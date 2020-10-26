package com.pghaz.revery.repository

import androidx.room.Dao
import androidx.room.RoomDatabase
import com.pghaz.revery.model.room.RAlarm

@Dao
abstract class AlarmDao(roomDatabase: RoomDatabase) :
    AbstractDao<RAlarm>(ReveryDatabase.TABLE_NAME_ALARM, roomDatabase)