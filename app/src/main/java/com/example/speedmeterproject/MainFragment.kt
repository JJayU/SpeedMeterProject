package com.example.speedmeterproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.preference.PreferenceManager
import com.example.speedmeterproject.databinding.ActivityMainBinding
import com.example.speedmeterproject.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private lateinit var binding : FragmentMainBinding
    private lateinit var bluetoothBridge : BluetoothBridge

    private var firstLaunch = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("a", "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if(firstLaunch) {
            firstLaunch = false

            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)

            bluetoothBridge = BluetoothBridge(this.requireContext(), binding)
            bluetoothBridge.start()

            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.requireContext())
            val autoConnect = sharedPreferences.getBoolean("auto_connect", false)

            binding.ConnectButton.setOnClickListener() {
                if(bluetoothBridge.connectedSuccessfully) {
                    if(!bluetoothBridge.activityRecorder.isRecording()) {
                        bluetoothBridge.stop()
                        binding.ConnectButton.text = getString(R.string.no_device_connected)
                    }
                } else {
                    bluetoothBridge.connectDevice()
                }
            }

            binding.startButton.setOnClickListener() {
                bluetoothBridge.startButtonPressed()
            }

            binding.saveButton.setOnClickListener() {
                bluetoothBridge.activityRecorder.saveToFile()
            }

            // TODO -> remove this
            bluetoothBridge.setMacAddress("98:D3:31:F4:03:F5") //temporary
            if(autoConnect) {
                bluetoothBridge.connectDevice()
            }
            //bluetoothBridge.activityRecorder.start()
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothBridge.stop()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        bluetoothBridge.permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults, this.requireContext())
    }

}