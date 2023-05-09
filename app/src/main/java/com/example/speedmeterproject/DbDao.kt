package com.example.speedmeterproject

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DbDao {

    @Insert
    suspend fun insertAll(activities : List<DbActivityItem>)

    @Delete
    suspend fun delete(activities : List<DbActivityItem>)

    @Query("SELECT * FROM activities_table")
    fun getAll() : List<DbActivityItem>

    @Query("DELETE FROM activities_table")
    suspend fun deleteAll()

}