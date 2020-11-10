package com.pghaz.revery.repository

import androidx.room.Dao
import androidx.room.RoomDatabase
import com.pghaz.revery.model.room.RTimer

@Dao
abstract class TimerDao(roomDatabase: RoomDatabase) :
    AbstractDao<RTimer>(ReveryDatabase.TABLE_NAME_TIMER, roomDatabase)