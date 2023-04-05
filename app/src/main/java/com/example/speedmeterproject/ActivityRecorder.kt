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

                val xmlSerializer = Xml.newSerializer()
                val writer = StringWriter()

                // Prepare .tcx file content
                xmlSerializer.apply {
                    setOutput(writer)
                    startDocument("UTF-8", true)
                    startTag("", "TrainingCenterDatabase")
                    attribute("", "xsi:schemaLocation", "http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2 http://www.garmin.com/xmlschemas/TrainingCenterDatabasev2.xsd")
                    attribute("", "xmlns:ns5", "http://www.garmin.com/xmlschemas/ActivityGoals/v1")
                    attribute("", "xmlns:ns3", "http://www.garmin.com/xmlschemas/ActivityExtension/v2")
                    attribute("", "xmlns:ns2", "http://www.garmin.com/xmlschemas/UserProfile/v2")
                    attribute("", "xmlns", "http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2")
                    attribute("", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
                    attribute("", "xmlns:ns4", "http://www.garmin.com/xmlschemas/ProfileExtension/v1")
                        startTag("", "Activities")
                            startTag("", "Activity")
                            attribute("", "Sport", "Biking")
                                startTag("", "Id")
                                text(activityID)
                                endTag("", "Id")
                                startTag("", "Lap")
                                attribute("", "StartTime", activityID)
                                    startTag("", "TotalTimeSeconds")
                                    text(((System.currentTimeMillis() - timeAtStart)/1000.0).toString())
                                    endTag("", "TotalTimeSeconds")
                                    startTag("", "DistanceMeters")
                                    text((distance*1000.0).toString())
                                    endTag("", "DistanceMeters")
                                    //TODO -> maximum speed
                                    startTag("", "Intensity")
                                    text("Active")
                                    endTag("", "Intensity")
                                    startTag("", "TriggerMethod")
                                    text("Manual")
                                    endTag("", "TriggerMethod")
                                    startTag("", "Track")
                                        for(trackpoint in trackpointList) {
                                            startTag("", "Trackpoint")
                                            startTag("", "Time")
                                            text(trackpoint.time.toString())
                                            endTag("", "Time")
                                            startTag("", "DistanceMeters")
                                            text((trackpoint.distance*1000.0).toString())
                                            endTag("", "DistanceMeters")
                                            endTag("", "Trackpoint")
                                        }
                                    endTag("", "Track")
                                endTag("", "Lap")
                            endTag("", "Activity")
                        endTag("", "Activities")
                    endTag("", "TrainingCenterDatabase")
                    endDocument()
                }

                file.writeText(writer.toString())
                Log.i("AR", "File saved!")
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