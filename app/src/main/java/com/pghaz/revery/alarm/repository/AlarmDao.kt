package com.pghaz.revery.alarm.repository

import androidx.lifecycle.LiveData
import androidx.room.*
import com.pghaz.revery.alarm.model.room.RAlarm

@Dao
interface AlarmDao {
    @Insert
    fun insert(alarm: RAlarm): Long

    @Query("SELECT * FROM " + AlarmDatabase.ALARM_TABLE_NAME + " ORDER BY id ASC")
    fun getAlarms(): LiveData<List<RAlarm>>

    @Query("SELECT * FROM " + AlarmDatabase.ALARM_TABLE_NAME + " WHERE id=:id")
    fun get(id: Long): LiveData<RAlarm>

    @Update
    fun update(alarm: RAlarm)

    @Query("DELETE FROM " + AlarmDatabase.ALARM_TABLE_NAME)
    fun deleteAll()

    @Delete
    fun delete(alarm: RAlarm)
}