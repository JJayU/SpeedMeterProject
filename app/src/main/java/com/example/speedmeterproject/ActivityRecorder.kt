package com.example.speedmeterproject

import android.content.Context
import android.os.Environment
import android.util.Log
import android.util.Xml
import android.view.View
import android.widget.Toast
import com.example.speedmeterproject.databinding.ActivityMainBinding
import java.io.File
import java.io.StringWriter
import java.time.LocalDateTime

/**
 * Class to record and process an activity
 */
class ActivityRecorder(private val context: Context, private var binding: ActivityMainBinding) {

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
    /** True if activity is saved */
    private var saved = false

    /** UTC Time of activity start */
    private var timeAtStart = 0L
    /** UTC Time of activity end */
    private var timeAtStop = 0L
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
            saved = false
            binding.saveButton.visibility = View.GONE
        }
    }

    /**
     * Stops recording an activity
     */
    fun stop() {
        if(recording) {
            recording = false
            timeAtStop = System.currentTimeMillis()
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
     * Return true if activity is already saved
     */
    fun isSaved() : Boolean {
        return saved
    }

    /**
     * Returns true if there aren't any points recorded
     */
    fun isEmpty() : Boolean {
        return trackpointList.isEmpty()
    }

    /**
     * Adds a new trackpoint to the activity when "recording" is true
     * @param receivedTimeOfRev time of a single wheel revolution received from the device
     */
    fun addTrackpoint(receivedTimeOfRev : Double) {
        if(recording) {
            val actualTimeMillis = System.currentTimeMillis()
            val actualTime = LocalDateTime.now()
            currentSpeed = 0.0

            if(lastTimeReceivedMillis != 0L && receivedTimeOfRev < 9999){
                val diffTime: Long = actualTimeMillis - lastTimeReceivedMillis
                val measuredDistance = diffTime/receivedTimeOfRev * 2.2                 //TODO -> change to configured wheel circumference
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

    /**
     * Updates GUI with updated values
     */
    private fun updateGUI(){
        binding.tripTime.text = time
        binding.actualSpeed.text = String.format("%04.1f", currentSpeed)
        binding.avgSpeed.text = String.format("%04.1f", avgSpeed)
        binding.tripDistance.text = String.format("%05.2f", distance)
    }

    /**
     * Saves current activity to .tcx file
     */
    fun saveToFile() {
        // Request code for creating a PDF document.
        Log.i("AR", "Saving file")

        if(isExternalStorageWritable()) {
            val activityID = LocalDateTime.now().toString()
            val file = File(context.getExternalFilesDir(null), "$activityID.tcx")
            file.createNewFile()

            if(file.isFile) {
                Log.i("AR", "File created successfully!")
                file.writeText(XmlGenerator().generateTCX(trackpointList, activityID, timeAtStart, timeAtStop, distance))
                Log.i("AR", "File saved!")
                saved = true
            } else {
                Log.i("AR", "File couldn't be created!")
            }
        } else {
            Toast.makeText(context, "No access to external storage!", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Checks if device have access to external storage
     */
    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }
}