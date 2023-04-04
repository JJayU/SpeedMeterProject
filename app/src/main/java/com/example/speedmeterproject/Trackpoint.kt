package com.example.speedmeterproject

/**
 * Data class to store trackpoint
 * @param time Date and time in ISO-8601
 * @param distance Distance at that trackpoint in meters
 */
data class Trackpoint(val time : String, val distance : Double)