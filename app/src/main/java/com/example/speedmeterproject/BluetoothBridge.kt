package com.example.speedmeterproject

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.speedmeterproject.databinding.ActivityMainBinding
import com.example.speedmeterproject.databinding.FragmentMainBinding
import com.harrysoft.androidbluetoothserial.BluetoothManager
import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Class that provides an interface to Bluetooth device
 * Uses BluetoothManager library from https://github.com/harry1453/android-bluetooth-serial
 */
class BluetoothBridge(private val context: Context, private var binding: FragmentMainBinding) {

    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var deviceInterface : SimpleBluetoothDeviceInterface

    private var compositeDisposable = CompositeDisposable()
    private lateinit var btDisposable : Disposable

    var permissionManager : PermissionManager = PermissionManager()

    /** MAC Address of the device to connect to */
    private var connectedDeviceMAC : String? = null
    /** Device connected */
    var connectedSuccessfully = false

    var activityRecorder = ActivityRecorder(context, binding)

    /**
     * Creates BluetoothManager instance and run permissions check
     */
    fun start() {
        bluetoothManager = BluetoothManager.getInstance()
        permissionManager.onStartupCheck(context)
    }

    /**
     * Closes connection and service
     */
    fun stop() {
        bluetoothManager.close()
        bluetoothManager.closeDevice(connectedDeviceMAC)
        connectedSuccessfully = false
    }

    /**
     * Sets MAC address of the device to connect
     * @param mac MAC Address of the device
     */
    fun setMacAddress(mac: String) {
        connectedDeviceMAC = mac
    }

    /**
     * Connects to the device with address provided by setMacAddress() method
     */
    fun connectDevice() {
        if (connectedDeviceMAC == null) {
            Log.e("BluetoothBridge", "No MAC Address Set!")
            return
        }
        else if (!permissionManager.hasBluetoothPermission(context)) {
            Log.w("BluetoothBridge", "No Bluetooth Permission!")
            Toast.makeText(context, context.getString(R.string.no_permission_bluetooth), Toast.LENGTH_LONG).show()
            return
        }
        else {
            btDisposable = bluetoothManager.openSerialDevice(connectedDeviceMAC)
                .subscribeOn(Schedulers.io())
                .subscribe(this::onConnected, this::onError)
            compositeDisposable.add(btDisposable)

            Toast.makeText(context, context.getString(R.string.connecting), Toast.LENGTH_SHORT).show()
            connectedSuccessfully = false
        }
    }

    /**
     * Executed when device is connected
     */
    private fun onConnected(connectedDevice : BluetoothSerialDevice) {
        deviceInterface = connectedDevice.toSimpleDeviceInterface()
        deviceInterface.setListeners(this::onMessageReceived, this::onMessageSent, this::onError)
    }

    /**
     * Executed every time a message is sent to device
     */
    private fun onMessageSent(message : String) {
        //Toast.makeText(this, "Sent a message!", Toast.LENGTH_SHORT).show()
    }

    /**
     * Executed every time a message is received
     */
    private fun onMessageReceived(message: String) {
        if(!connectedSuccessfully) {
            connectedSuccessfully = true
            Toast.makeText(context, context.getString(R.string.connected), Toast.LENGTH_SHORT).show()
            binding.ConnectButton.text = getCurrentDeviceName()
        }

        val receivedTimeOfRev = message.toDoubleOrNull()

        if(receivedTimeOfRev != null){
            activityRecorder.addTrackpoint(receivedTimeOfRev)
        }
        else {
            Log.i("BT Bridge", "Incorrect message received!")
        }
    }

    /**
     * Executed on error
     */
    private fun onError(error : Throwable) {
        return
    }

    /**
     * Lists all paired bluetooth devices and print them to the log
     */
    @SuppressLint("MissingPermission")
    fun listBluetoothDevices() {
        if (!permissionManager.hasBluetoothPermission(context)) {
            Toast.makeText(context, context.getString(R.string.no_permission_bluetooth), Toast.LENGTH_LONG).show()
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

    @SuppressLint("MissingPermission")
    fun getCurrentDeviceName() : String {
        val pairedDevices : List<BluetoothDevice> = bluetoothManager.pairedDevicesList
        for (device in pairedDevices) {
            if (connectedDeviceMAC == device.address) {
                return device.name
            }
        }
        return ""
    }

    fun startButtonPressed() {
        if ( !activityRecorder.isRecording() && connectedSuccessfully ) {
            if(!activityRecorder.isSaved() && !activityRecorder.isEmpty()) {
                Toast.makeText(context, context.getString(R.string.activity_not_saved_yet), Toast.LENGTH_LONG).show()

                val alertDialogBuilder = AlertDialog.Builder(context)
                alertDialogBuilder.setTitle(context.getString(R.string.activity_not_saved))
                alertDialogBuilder.setMessage(context.getString(R.string.activity_not_saved_desc))
                alertDialogBuilder.setPositiveButton(context.getString(R.string.yes)) { _, _ ->
                    activityRecorder.start()
                    binding.startButton.text = context.getString(R.string.stop)
                }
                alertDialogBuilder.setNegativeButton(context.getString(R.string.no)) { _, _ ->

                }
                alertDialogBuilder.show()
            }
            else {
                activityRecorder.start()
                binding.startButton.text = context.getString(R.string.stop)
            }
        }
        else {
            activityRecorder.stop()
            binding.startButton.text = context.getString(R.string.start)
        }
    }

}
