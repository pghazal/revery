package com.pghaz.revery.repository

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AlarmDao {
    @Insert
    fun insert(alarm: Alarm): Long

    @Query("SELECT * FROM " + AlarmDatabase.ALARM_TABLE_NAME + " ORDER BY id ASC")
    fun getAlarms(): LiveData<List<Alarm>>

    @Query("SELECT * FROM " + AlarmDatabase.ALARM_TABLE_NAME + " WHERE id=:id")
    fun get(id: Long): LiveData<Alarm> // we're not using id because we don't get one at creation of pending intent

    @Update
    fun update(alarm: Alarm)

    @Query("DELETE FROM " + AlarmDatabase.ALARM_TABLE_NAME)
    fun deleteAll()

    @Delete
    fun delete(alarm: Alarm)
}