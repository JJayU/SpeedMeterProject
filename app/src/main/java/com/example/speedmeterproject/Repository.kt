package com.example.speedmeterproject

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/*
 Repository for accessing databaase
 */
class Repository(context: Context) : DbDao {
    private val dao = ActivitiesDb.getInstance(context).dbDao()

    // Add a list of activities to database
    override suspend fun insertAll(activities: List<DbActivityItem>) = withContext(Dispatchers.IO){
        dao.insertAll(activities)
    }

    // Delete a list of activities from database
    override suspend fun delete(activities: List<DbActivityItem>) = withContext(Dispatchers.IO){
        dao.delete(activities)
    }

    // Returns a list of DbActivityItem from database
    override fun getAll(): List<DbActivityItem> {
        return dao.getAll()
    }

    // Deletes all activities from database
    override suspend fun deleteAll() = withContext(Dispatchers.IO){
        dao.deleteAll()
    }
}