package com.example.speedmeterproject

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.harrysoft.androidbluetoothserial.BluetoothManager
import androidx.databinding.DataBindingUtil
import com.example.speedmeterproject.databinding.ActivityMainBinding
import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice;
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var deviceInterface : SimpleBluetoothDeviceInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 1)
            //Toast.makeText(this, "No permission for Bluetooth!", Toast.LENGTH_LONG).show()
            //finish()
        }

        bluetoothManager = BluetoothManager.getInstance()
//        if (bluetoothManager == null) {
//            Toast.makeText(this, "Bluetooth not available!", Toast.LENGTH_LONG).show()
//            finish()
//        }

        binding.searchDevicesButton.setOnClickListener() {
            ListBluetoothDevices()
        }

        binding.ConnectButton.setOnClickListener() {
            connectDevice("98:D3:31:F4:03:F5")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothManager.close()
        bluetoothManager.closeDevice("98:D3:31:F4:03:F5")
    }

    private fun ListBluetoothDevices() {
        val pairedDevices : List<BluetoothDevice> = bluetoothManager.pairedDevicesList

        for (device in pairedDevices) {

            Log.d("BT Test", "Device Name: " + device.name)
            Log.d("BT Test", "MAC: " + device.address)
        }
    }

    private fun connectDevice(mac : String) {
        bluetoothManager.openSerialDevice(mac)
            .subscribeOn(Schedulers.io())
            //.observeOn(Schedulers.mainThread())
            .subscribe(this::onConnected, this::onError);
    }

    private fun onConnected(connectedDevice : BluetoothSerialDevice) {
        deviceInterface = connectedDevice.toSimpleDeviceInterface()
        deviceInterface.setListeners(this::onMessageReceived, this::onMessageSent, this::onError)
        deviceInterface.sendMessage("Hello :)")
    }

    private fun onMessageSent(message : String) {
        Toast.makeText(this, "Sent a message!", Toast.LENGTH_LONG).show()
    }

    private fun onMessageReceived(message: String) {
        Toast.makeText(this, "Received: $message", Toast.LENGTH_LONG).show()
    }

    private fun onError(error : Throwable) {
        return
    }
}
