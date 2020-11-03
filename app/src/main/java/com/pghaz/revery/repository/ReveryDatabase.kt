package com.pghaz.revery.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pghaz.revery.model.room.RAlarm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

@Database(entities = [RAlarm::class], version = 2, exportSchema = false)
abstract class ReveryDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao

    companion object {
        private const val DATABASE_NAME = "pghaz_revery_database"

        const val TABLE_NAME_ALARM = "revery_table_alarm"

        private val job = Job()
        val databaseCoroutinesScope: CoroutineScope = CoroutineScope(job + Dispatchers.IO)

        @Volatile
        private lateinit var INSTANCE: ReveryDatabase

        fun getDatabase(context: Context): ReveryDatabase {
            if (!this::INSTANCE.isInitialized) {
                synchronized(ReveryDatabase::class.java) {
                    if (!this::INSTANCE.isInitialized) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            ReveryDatabase::class.java,
                            DATABASE_NAME
                        ).addMigrations(MIGRATION_1_2).build()
                    }
                }
            }
            return INSTANCE
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE $TABLE_NAME_ALARM ADD COLUMN repeat INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}