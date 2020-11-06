package com.pghaz.revery.repository

import androidx.lifecycle.LiveData
import androidx.room.*
import com.pghaz.revery.model.room.RAlarm

@Dao
interface AlarmDao {
    @Insert
    fun insert(alarm: RAlarm): Long

    @Query("SELECT * FROM " + ReveryDatabase.TABLE_NAME_ALARM + " ORDER BY id ASC")
    fun getAlarms(): LiveData<List<RAlarm>>

    @Query("SELECT * FROM " + ReveryDatabase.TABLE_NAME_ALARM + " WHERE id=:id")
    fun get(id: Long): LiveData<RAlarm> // we're not using id because we don't get one at creation of pending intent

    @Update
    fun update(alarm: RAlarm)

    @Query("DELETE FROM " + ReveryDatabase.TABLE_NAME_ALARM)
    fun deleteAll()

    @Delete
    fun delete(alarm: RAlarm)
}