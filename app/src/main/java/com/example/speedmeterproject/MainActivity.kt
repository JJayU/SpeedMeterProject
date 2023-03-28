package com.example.speedmeterproject

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.harrysoft.androidbluetoothserial.BluetoothManager
import androidx.databinding.DataBindingUtil
import com.example.speedmeterproject.databinding.ActivityMainBinding
import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice;
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import com.example.speedmeterproject.BluetoothBridge

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var bluetoothBridge : BluetoothBridge

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        bluetoothBridge = BluetoothBridge(this, binding)
        bluetoothBridge.start()

        binding.searchDevicesButton.setOnClickListener() {
            bluetoothBridge.listBluetoothDevices()
        }

        binding.ConnectButton.setOnClickListener() {
            bluetoothBridge.setMacAddress("98:D3:31:F4:03:F5") //temporary
            bluetoothBridge.connectDevice()
        }
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
