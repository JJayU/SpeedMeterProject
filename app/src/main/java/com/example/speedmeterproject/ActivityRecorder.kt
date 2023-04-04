package com.example.speedmeterproject

import android.opengl.Visibility
import android.util.Log
import android.view.View
import com.example.speedmeterproject.Trackpoint
import com.example.speedmeterproject.databinding.ActivityMainBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Class to record and process an activity
 */
class ActivityRecorder(private var binding: ActivityMainBinding) {

    /** Time from the beginning of activity */
    var time = ""
    /** Total distance in activity */
    var distance = 0.0
    /** Average speed in activity */
    var avgSpeed = 0.0
    /** Current speed */
    var currentSpeed = 0.0

    /** True if activity is being recorded */
    private var recording = false

    /** UTC Time of activity start */
    private var timeAtStart = 0L
    /** UTC Time of last message received */
    private var lastTimeReceivedMillis = 0L

    /** List of all trackpoints */
    private var trackpointList = mutableListOf<Trackpoint>()

    /**
     * Starts recording an activity, resets all activity data
     */
    fun start(){
        if(!recording) {
            timeAtStart = System.currentTimeMillis()
            time = "00:00:00"
            distance = 0.0
            avgSpeed = 0.0
            trackpointList.clear()
            recording = true
            binding.saveButton.visibility = View.GONE
        }
    }

    /**
     * Stops recording an activity
     */
    fun stop() {
        if(recording) {
            recording = false
            if(trackpointList.size > 0) {
                binding.saveButton.visibility = View.VISIBLE
            }
            binding.actualSpeed.text = "00,0"
        }
    }

    /**
     * Returns true if activity is currently being recorded
     */
    fun isRecording() : Boolean {
        return recording
    }

    /**
     * Adds a new trackpoint to the activity when "recording" is true
     * @param receivedTimeOfRev time of a single wheel revolution received from the device
     */
    fun addTrackpoint(receivedTimeOfRev : Double) {
        if(recording) {
            val actualTimeMillis = System.currentTimeMillis()
            var diffTime = 0L
            var measuredDistance = 0.0
            val actualTime = LocalDateTime.now()
            currentSpeed = 0.0

            if(lastTimeReceivedMillis != 0L && receivedTimeOfRev < 9999){
                diffTime = actualTimeMillis - lastTimeReceivedMillis
                measuredDistance = diffTime/receivedTimeOfRev * 2.2                                     //TODO -> change to configured wheel circumference
                currentSpeed = (measuredDistance/1000.0) / (diffTime/1000.0/60.0/60.0)

                distance += measuredDistance/1000.0
                avgSpeed = (avgSpeed*trackpointList.size + currentSpeed)/(trackpointList.size+1)
            }

            lastTimeReceivedMillis = actualTimeMillis

            val elapsedTime = actualTimeMillis - timeAtStart
            val hours = (elapsedTime/1000.0/60.0/60.0).toInt()
            val minutes = (elapsedTime/1000.0/60.0).toInt() % 60
            val seconds = (elapsedTime/1000.0).toInt() % 60

            time = String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds) //TODO -> clean

            trackpointList.add(Trackpoint(actualTime.toString(), distance))

            updateGUI()
        }
    }

    private fun updateGUI(){
        binding.tripTime.text = time
        binding.actualSpeed.text = String.format("%04.1f", currentSpeed)
        binding.avgSpeed.text = String.format("%04.1f", avgSpeed)
        binding.tripDistance.text = String.format("%05.2f", distance)
    }

}