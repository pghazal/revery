package com.pghaz.revery.alarm.repository

import android.annotation.SuppressLint
import androidx.lifecycle.ComputableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.pghaz.revery.alarm.model.RBaseModel

abstract class AbstractDao<T : RBaseModel>(
    private val tableName: String,
    private val roomDatabase: RoomDatabase
) {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(entity: T): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(entities: List<T>): LongArray

    @Update
    abstract fun update(entity: T)

    @Update
    abstract fun update(entities: List<T>)

    @Delete
    abstract fun delete(entity: T)

    @Delete
    abstract fun delete(entities: List<T>)

    @RawQuery
    protected abstract fun deleteAll(query: SupportSQLiteQuery): Int

    fun deleteAll() {
        val query = SimpleSQLiteQuery("DELETE FROM $tableName")
        deleteAll(query)
    }

    @RawQuery
    protected abstract fun getEntitySync(query: SupportSQLiteQuery): List<T>?

    fun getEntitySync(id: Long): T? {
        return getEntitySync(listOf(id)).firstOrNull()
    }

    fun getEntitySync(ids: List<Long>): List<T> {
        val result = StringBuilder()

        for (index in ids.indices) {
            if (index != 0) {
                result.append(",")
            }
            result.append("'").append(ids[index]).append("'")
        }

        val query = SimpleSQLiteQuery("SELECT * FROM $tableName WHERE id IN ($result);")

        return getEntitySync(query) ?: ArrayList()
    }

    fun getEntity(id: Long): LiveData<T> {
        val resultLiveData = MediatorLiveData<T>()

        resultLiveData.addSource(getEntity(listOf(id))) { obj ->
            resultLiveData.postValue(obj?.firstOrNull())
        }

        return resultLiveData
    }

    @SuppressLint("RestrictedApi")
    fun getEntity(ids: List<Long>): LiveData<List<T>> {
        return object : ComputableLiveData<List<T>>() {
            private var observer: InvalidationTracker.Observer? = null

            override fun compute(): List<T>? {
                if (observer == null) {
                    observer = object : InvalidationTracker.Observer(tableName) {
                        override fun onInvalidated(tables: Set<String>) = invalidate()
                    }
                    roomDatabase.invalidationTracker.addWeakObserver(observer)
                }
                return getEntitySync(ids)
            }
        }.liveData
    }

    fun getEntitiesSync(): List<T> {
        val query = SimpleSQLiteQuery("SELECT * FROM $tableName ORDER BY id ASC;")
        return getEntitySync(query) ?: ArrayList()
    }

    @SuppressLint("RestrictedApi")
    fun getEntities(): LiveData<List<T>> {
        return object : ComputableLiveData<List<T>>() {
            private var observer: InvalidationTracker.Observer? = null

            override fun compute(): List<T>? {
                if (observer == null) {
                    observer = object : InvalidationTracker.Observer(tableName) {
                        override fun onInvalidated(tables: Set<String>) = invalidate()
                    }
                    roomDatabase.invalidationTracker.addWeakObserver(observer)
                }
                return getEntitiesSync()
            }
        }.liveData
    }
}