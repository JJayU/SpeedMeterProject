package com.example.speedmeterproject

import android.app.ActionBar.LayoutParams
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.example.speedmeterproject.databinding.FragmentMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.time.LocalDateTime
import kotlin.random.Random

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

            time = String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds)

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
    suspend fun saveButtonClicked() {
        // Check if can save to storage
        if(isExternalStorageWritable()) {
            val activityID = LocalDateTime.now().toString()
            var activityName = activityID

            // Setup and create a dialog
            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.name_picker_dialog)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
            dialog.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

            // Setup edit text field
            val activityNameEditText =  dialog.findViewById<EditText>(R.id.activityName)
            activityNameEditText.hint = activityName

            // On save button clicked
            dialog.findViewById<Button>(R.id.save_button).setOnClickListener {
                val textFromEditText = activityNameEditText.text.toString()
                if(textFromEditText != "") {
                    activityName = textFromEditText
                }

                // Write to file 2.0
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.IO) {
                        val docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                        val appDir = File(docsDir, "SpeedMeterApp")
                        appDir.mkdirs()
                        val file2 = File(appDir, "${filterFilename(activityName)}.tcx")
                        val outputStream = FileOutputStream(file2)
                        val writer = OutputStreamWriter(outputStream)
                        writer.write(XmlGenerator().generateTCX(trackpointList, activityID, timeAtStart, timeAtStop, distance))
                        writer.close()

                        if(!file2.isFile) {
                            Log.e("ActivityRecorder", "File couldn't be created!")
                        }
                    }
                }

                // Write to database
                val distanceToSave = String.format("%05.2f", distance)
                val avgSpeedToSave = String.format("%04.1f", avgSpeed)
                val repo = Repository(context)
                CoroutineScope(Dispatchers.IO).launch {
                    repo.insertAll(listOf(DbActivityItem(name = activityName, distance = distanceToSave, time = time, avgSpeed = avgSpeedToSave, date = activityID)))
                }

                dialog.dismiss()
                binding.saveButton.visibility = View.INVISIBLE
                Toast.makeText(context, R.string.file_saved, Toast.LENGTH_LONG).show()
            }
            dialog.findViewById<Button>(R.id.cancel_button).setOnClickListener {
                dialog.dismiss()
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

    /*
     * Filter file name so it is an allowed file name in Android
     */
    private fun filterFilename(filename: String): String {
        val filteredFilename = filename.replace("[^a-zA-Z0-9.-]".toRegex(), "")

        // Limit to 255 characters
        return if (filteredFilename.length > 255) {
            filteredFilename.substring(0, 255)
        }
        else if (filteredFilename == "") {
            Random.nextInt(1000, 100000).toString()
        }
        else {
            filteredFilename
        }
    }


    private fun saveToDbAndFile() {

    }
}
