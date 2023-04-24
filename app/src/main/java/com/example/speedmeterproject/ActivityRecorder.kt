package com.example.speedmeterproject

import android.content.Context
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.example.speedmeterproject.databinding.FragmentMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime

/**
 * Class to record and process an activity
 */
class ActivityRecorder(private val context: Context, private var binding: FragmentMainBinding) {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

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
            binding.saveButton.visibility = View.INVISIBLE
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
                val wheelCircumference = sharedPreferences.getString("wheel_circ", "")?.toDoubleOrNull() ?: return
                val measuredDistance = diffTime/receivedTimeOfRev * (wheelCircumference/1000.0)
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
    suspend fun saveToFile() {
        // Check if can save to storage
        if(isExternalStorageWritable()) {
            val activityID = LocalDateTime.now().toString()
            val file = File(context.getExternalFilesDir(null), "$activityID.tcx")
            withContext(Dispatchers.IO) {
                file.createNewFile()
            }

            if(file.isFile) {
                // Write data to file
                file.writeText(XmlGenerator().generateTCX(trackpointList, activityID, timeAtStart, timeAtStop, distance))
                Toast.makeText(context, R.string.file_saved, Toast.LENGTH_LONG).show()

                // Write to database
                val distanceToSave = String.format("%05.2f", distance)
                val avgSpeedToSave = String.format("%04.1f", avgSpeed)
                val repo = Repository(context)
                repo.insertAll(listOf(DbActivityItem(name = activityID, distance = distanceToSave, time = time, avgSpeed = avgSpeedToSave)))

                saved = true
            } else {
                Log.i("AR", "File couldn't be created!")
            }
        } else {
            Toast.makeText(context, R.string.no_storage_access, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Checks if device have access to external storage
     */
    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }
}
