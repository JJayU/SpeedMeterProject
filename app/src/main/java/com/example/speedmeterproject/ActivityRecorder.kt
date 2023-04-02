package com.example.speedmeterproject

import android.opengl.Visibility
import android.util.Log
import android.view.View
import com.example.speedmeterproject.Trackpoint
import com.example.speedmeterproject.databinding.ActivityMainBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ActivityRecorder(private var binding: ActivityMainBinding) {

    var time : String = ""
    var distance : Double = 0.0
    var avgSpeed : Double = 0.0

    private var recording = false

    private var timeAtStart = 0L
    private var lastTimeReceivedMillis = 0L

    private var trackpointList = mutableListOf<Trackpoint>()

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

    fun stop() {
        if(recording) {
            recording = false
            if(trackpointList.size > 0) {
                binding.saveButton.visibility = View.VISIBLE
            }
            binding.actualSpeed.text = "00,0"
        }
    }

    fun isRecording() : Boolean {
        return recording
    }

    fun addTrackpoint(receivedTimeOfRev : Double) {
        if(recording) {
            val actualTimeMillis = System.currentTimeMillis()
            var diffTime = 0L
            var measuredDistance = 0.0
            var measuredSpeed = 0.0
            val actualTime = LocalDateTime.now()

            if(lastTimeReceivedMillis != 0L){
                diffTime = actualTimeMillis - lastTimeReceivedMillis
                measuredDistance = diffTime/receivedTimeOfRev * 2.2                                     //TODO -> change to configured wheel circumference
                measuredSpeed = (measuredDistance/1000.0) / (diffTime/1000.0/60.0/60.0)

                distance += measuredDistance/1000.0
                avgSpeed = (avgSpeed*trackpointList.size + measuredSpeed)/(trackpointList.size+1)
            }

            lastTimeReceivedMillis = actualTimeMillis

            val elapsedTime = actualTimeMillis - timeAtStart
            val hours = (elapsedTime/1000.0/60.0/60.0).toInt()
            val minutes = (elapsedTime/1000.0/60.0).toInt() % 60
            val seconds = (elapsedTime/1000.0).toInt() % 60

            binding.tripTime.text = String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds) //TODO -> clean
            binding.actualSpeed.text = String.format("%04.1f", measuredSpeed)
            binding.avgSpeed.text = String.format("%04.1f", avgSpeed)
            binding.tripDistance.text = String.format("%05.2f", distance)

            //Log.i("BT Bridge", measuredSpeed.toString())

            trackpointList.add(Trackpoint(actualTime.toString(), distance))
        }
    }

}