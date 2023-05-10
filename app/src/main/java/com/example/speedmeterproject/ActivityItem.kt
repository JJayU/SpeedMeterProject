package com.example.speedmeterproject

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Data class for storing activity statistics in database */
@Entity(tableName = "activities_table")
data class DbActivityItem(
    @PrimaryKey(autoGenerate = true) val uid : Int = 0,
    val name : String,
    val distance : String,
    val time : String,
    val avgSpeed : String,
    val date : String)