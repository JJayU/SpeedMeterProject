package com.example.speedmeterproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import com.example.speedmeterproject.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var bluetoothBridge : BluetoothBridge

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        bluetoothBridge = BluetoothBridge(this, binding)
        bluetoothBridge.start()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
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

        binding.settingsButton.setOnClickListener() {
            val settingsActivity = Intent(this, SettingsActivity::class.java)
            startActivity(settingsActivity)
        }

        // TODO -> remove this
        bluetoothBridge.setMacAddress("98:D3:31:F4:03:F5") //temporary
        if(autoConnect) {
            bluetoothBridge.connectDevice()
        }
        //bluetoothBridge.activityRecorder.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothBridge.stop()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        bluetoothBridge.permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}
