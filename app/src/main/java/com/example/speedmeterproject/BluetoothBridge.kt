package com.example.speedmeterproject

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Looper
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.example.speedmeterproject.databinding.ActivityMainBinding
import com.harrysoft.androidbluetoothserial.BluetoothManager
import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay

class BluetoothBridge(private val context: Context, private var binding: ActivityMainBinding) {

    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var deviceInterface : SimpleBluetoothDeviceInterface

    private var compositeDisposable = CompositeDisposable()
    private lateinit var btDisposable : Disposable

    var permissionManager : PermissionManager = PermissionManager()

    private var connectedDeviceMAC : String? = null
    private var connectedSuccessfully = false

    fun start() {
        bluetoothManager = BluetoothManager.getInstance()
        permissionManager.onStartupCheck(context)
    }

    fun stop() {
        bluetoothManager.close()
        bluetoothManager.closeDevice(connectedDeviceMAC)
    }

    fun setMacAddress(mac: String) {
        connectedDeviceMAC = mac
    }

    fun connectDevice() {
        if (connectedDeviceMAC == null) {
            Log.e("BluetoothBridge", "No MAC Address Set!")
            return
        }
        else if (!permissionManager.hasBluetoothPermission(context)) {
            Log.w("BluetoothBridge", "No Bluetooth Permission!")
            Toast.makeText(context, "Brak uprawnien do polaczenia bluetooth!", Toast.LENGTH_LONG).show()
            return
        }
        else {
            btDisposable = bluetoothManager.openSerialDevice(connectedDeviceMAC)
                .subscribeOn(Schedulers.io())
                .subscribe(this::onConnected, this::onError)
            compositeDisposable.add(btDisposable)

            Toast.makeText(context, "Connecting...", Toast.LENGTH_SHORT).show()
            connectedSuccessfully = false
        }
    }

    private fun onConnected(connectedDevice : BluetoothSerialDevice) {
        deviceInterface = connectedDevice.toSimpleDeviceInterface()
        deviceInterface.setListeners(this::onMessageReceived, this::onMessageSent, this::onError)
        //deviceInterface.sendMessage("Hello :)")
    }

    private fun onMessageSent(message : String) {
        //Toast.makeText(this, "Sent a message!", Toast.LENGTH_SHORT).show()
    }

    private fun onMessageReceived(message: String) {
        if(!connectedSuccessfully) {
            connectedSuccessfully = true
            Toast.makeText(context, "Connected!", Toast.LENGTH_SHORT).show()
        }

        //TODO -> check if message is correct
        binding.textView2.text = message
    }

    private fun onError(error : Throwable) {
        return
    }

    @SuppressLint("MissingPermission")
    fun listBluetoothDevices() {
        if (!permissionManager.hasBluetoothPermission(context)) {
            Toast.makeText(context, "Brak uprawnien do polaczenia bluetooth!", Toast.LENGTH_LONG).show()
            return
        }
        else {
            val pairedDevices : List<BluetoothDevice> = bluetoothManager.pairedDevicesList
            for (device in pairedDevices) {
                Log.d("BT Test", "Device Name: " + device.name)
                Log.d("BT Test", "MAC: " + device.address)
            }
        }
    }

}
