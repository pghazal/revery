package com.pghaz.revery.alarm.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pghaz.revery.alarm.model.room.RAlarm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

@Database(entities = [RAlarm::class], version = 1, exportSchema = false)
abstract class AlarmDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao

    companion object {
        private const val DATABASE_NAME = "revery_alarm_database"

        const val ALARM_TABLE_NAME = "revery_table_alarm"

        private val job = Job()
        val databaseCoroutinesScope: CoroutineScope = CoroutineScope(job + Dispatchers.IO)

        @Volatile
        private lateinit var INSTANCE: AlarmDatabase

        fun getDatabase(context: Context): AlarmDatabase {
            if (!this::INSTANCE.isInitialized) {
                synchronized(AlarmDatabase::class.java) {
                    if (!this::INSTANCE.isInitialized) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            AlarmDatabase::class.java,
                            DATABASE_NAME
                        ).build()
                    }
                }
            }
            return INSTANCE
        }
    }
}