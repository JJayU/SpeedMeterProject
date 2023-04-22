package com.example.speedmeterproject

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class Repository(context: Context) : DbDao {
    private val dao = ActivitiesDb.getInstance(context).dbDao()

    override suspend fun insertAll(activities: List<DbActivityItem>) = withContext(Dispatchers.IO){
        dao.insertAll(activities)
    }

    override suspend fun delete(activities: List<DbActivityItem>) = withContext(Dispatchers.IO){
        dao.delete(activities)
    }

    override fun getAll(): List<DbActivityItem> {
        return dao.getAll()
    }

    override suspend fun deleteAll() = withContext(Dispatchers.IO){
        dao.deleteAll()
    }
}