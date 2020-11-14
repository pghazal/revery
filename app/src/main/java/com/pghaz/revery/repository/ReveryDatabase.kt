package com.pghaz.revery.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pghaz.revery.model.room.RAlarm
import com.pghaz.revery.model.room.RTimer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

@Database(entities = [RAlarm::class, RTimer::class], version = 3, exportSchema = false)
abstract class ReveryDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao
    abstract fun timerDao(): TimerDao

    companion object {
        private const val DATABASE_NAME = "pghaz_revery_database"

        const val TABLE_NAME_ALARM = "revery_table_alarm"
        const val TABLE_NAME_TIMER = "revery_table_timer"

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
                        )
                            .addMigrations(MIGRATION_1_2)
                            .addMigrations(MIGRATION_2_3)
                            .build()
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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `revery_table_timer` (`id` INTEGER NOT NULL, `label` TEXT NOT NULL, `vibrate` INTEGER NOT NULL, `fadeOut` INTEGER NOT NULL, `fadeOutDuration` INTEGER NOT NULL, `duration` INTEGER NOT NULL, `startTime` INTEGER NOT NULL, `stopTime` INTEGER NOT NULL, `remainingTime` INTEGER NOT NULL, `state` INTEGER NOT NULL, `uri` TEXT, `href` TEXT, `type` INTEGER NOT NULL, `name` TEXT, `description` TEXT, `imageUrl` TEXT, `shuffle` INTEGER NOT NULL, `shouldKeepPlaying` INTEGER NOT NULL, `repeat` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
        }
    }
}