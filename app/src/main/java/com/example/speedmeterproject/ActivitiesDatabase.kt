package com.example.speedmeterproject

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


/*
 Declaration of database
 */
@Database(entities = [DbActivityItem::class], version = 1)
abstract class ActivitiesDatabase : RoomDatabase() {
    abstract fun dbDao() : DbDao
}

object ActivitiesDb {
    private var db : ActivitiesDatabase? = null

    fun getInstance(context: Context) : ActivitiesDatabase{
        // Prevents creating multiple database instances
        if(db == null) {
            db = Room.databaseBuilder(context, ActivitiesDatabase::class.java, "activities-database").allowMainThreadQueries().build()
        }
        return db!!
    }
}
