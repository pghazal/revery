package com.pghaz.revery.repository

import android.annotation.SuppressLint
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.ComputableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.pghaz.revery.model.room.RBaseModel

abstract class AbstractDao<T : RBaseModel>(
    private val tableName: String,
    private val roomDatabase: RoomDatabase
) {

    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(entity: T): Long

    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(entities: List<T>): LongArray

    @WorkerThread
    @Update
    abstract fun update(entity: T)

    @WorkerThread
    @Update
    abstract fun update(entities: List<T>)

    @WorkerThread
    @Delete
    abstract fun delete(entity: T)

    @WorkerThread
    @Delete
    abstract fun delete(entities: List<T>)

    @WorkerThread
    @RawQuery
    protected abstract fun executeSQLQuery(query: SupportSQLiteQuery): Int

    @WorkerThread
    fun deleteAll() {
        val query = SimpleSQLiteQuery("DELETE FROM $tableName")
        executeSQLQuery(query)
    }

    @MainThread
    @RawQuery
    protected abstract fun getEntitySync(query: SupportSQLiteQuery): List<T>?

    @MainThread
    fun getEntitySync(id: Long): T? {
        return getEntitySync(listOf(id)).firstOrNull()
    }

    @MainThread
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

    @MainThread
    fun getEntity(id: Long): LiveData<T> {
        val resultLiveData = MediatorLiveData<T>()

        resultLiveData.addSource(getEntity(listOf(id))) { obj ->
            resultLiveData.postValue(obj?.firstOrNull())
        }

        return resultLiveData
    }

    @MainThread
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

    @MainThread
    fun getEntitiesSync(): List<T> {
        val query = SimpleSQLiteQuery("SELECT * FROM $tableName ORDER BY id ASC;")
        return getEntitySync(query) ?: ArrayList()
    }

    @MainThread
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