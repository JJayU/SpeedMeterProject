package com.example.speedmeterproject

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
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

    private lateinit var deviceInterface : SimpleBluetoothDeviceInterface

    private var cyclicHandler = Handler(Looper.getMainLooper())

    /** Maximum time in millis after which activity is stopped when no connection */
    private val TIMEOUT_TIME = 5000

    private var compositeDisposable = CompositeDisposable()
    private lateinit var btDisposable : Disposable

    var permissionManager : PermissionManager = PermissionManager()

    /** MAC Address of the device to connect to */
    private var connectedDeviceMAC : String? = null
    /** Device connected */
    var connectedSuccessfully = false
    /** Data sent from Arduino update interval */
    private var updateInterval = 500

    private var lastTimeMessageReceived = 0L


    var activityRecorder = ActivityRecorder(context, binding)

    /**
     * Creates BluetoothManager instance and run permissions check
     */
    fun start() {
        bluetoothManager = BluetoothManager.getInstance()
        permissionManager.onStartupCheck(context)
        cyclicHandler.post(checkIfStillConnectedAndUpdateGUI)
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
        //Toast.makeText(context, "Sent a message!", Toast.LENGTH_SHORT).show()
    }

    /**
     * Executed every time a message is received
     */
    private fun onMessageReceived(message: String) {
        if(!connectedSuccessfully) {
            connectedSuccessfully = true
            Toast.makeText(context, context.getString(R.string.connected), Toast.LENGTH_SHORT).show()
            binding.ConnectButton.text = getCurrentDeviceName()
            sendNewUpdateInterval()
            binding.bluetoothStatus.setImageDrawable(context.getDrawable(R.drawable.baseline_bluetooth_connected_24))
        }

        val receivedTimeOfRev = message.toDoubleOrNull()

        if(receivedTimeOfRev != null){
            activityRecorder.addTrackpoint(receivedTimeOfRev)
        }
        else {
            Log.i("BT Bridge", "Incorrect message received!")
        }

        lastTimeMessageReceived = System.currentTimeMillis()
    }

    /**
     * Executed on error
     */
    private fun onError(error : Throwable) {
        return
    }

    @SuppressLint("MissingPermission")
    /**
     * Returns string containing current connected device name
     */
    fun getCurrentDeviceName() : String {
        val pairedDevices : List<BluetoothDevice> = bluetoothManager.pairedDevicesList
        for (device in pairedDevices) {
            if (connectedDeviceMAC == device.address) {
                return device.name
            }
        }
        return ""
    }

    /**
     * Starts or stops activity recording and updates button text
     * Additionally creates a dialog when user wants to start recording a new activity, without saving previous one
     */
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
                alertDialogBuilder.setNegativeButton(context.getString(R.string.no)) { _, _ -> }
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

    fun checkForPreferencesChange() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        // Check for change in speed unit
        val speedUnit = sharedPreferences.getString("speed_unit", "")
        if(speedUnit == "kmph") {
            binding.speedUnit.text = context.getString(R.string.speed_unit_kmph)
            activityRecorder.speedUnitMph = false
        }
        else {
            binding.speedUnit.text = context.getString(R.string.speed_unit_mph)
            activityRecorder.speedUnitMph = true
        }
        activityRecorder.updateGUI()

        // Check for change in update interval
        val newUpdateInterval = sharedPreferences.getString("update_interval", "")
        if(newUpdateInterval != "" && newUpdateInterval != null) {
            if(newUpdateInterval.toInt() != updateInterval) {
                updateInterval = newUpdateInterval.toInt()
                sendNewUpdateInterval()
            }
        }
    }

    fun sendNewUpdateInterval() {
        if(connectedSuccessfully) {
            val messageToSend = when (updateInterval) {
                250 -> "1"
                500 -> "2"
                750 -> "3"
                1000 -> "4"
                else -> "2"
            }
            deviceInterface.sendMessage(messageToSend)
        }
    }

    private val checkIfStillConnectedAndUpdateGUI = object : Runnable {
        override fun run() {
            activityRecorder.updateGUI()
            if(connectedSuccessfully) {
                val diffTime: Long = System.currentTimeMillis() - lastTimeMessageReceived
                if(diffTime > TIMEOUT_TIME) {
                    Toast.makeText(context, "Device disconnected!", Toast.LENGTH_LONG).show()
                    activityRecorder.stop()
                    connectedSuccessfully = false
                    bluetoothManager.closeDevice(connectedDeviceMAC)
                    binding.ConnectButton.text = context.getString(R.string.no_device_connected)
                    binding.startButton.text = context.getString(R.string.start)
                    binding.bluetoothStatus.setImageDrawable(context.getDrawable(R.drawable.baseline_bluetooth_disabled_24))
                }
            }
            cyclicHandler.postDelayed(this, 100)
        }
    }

    companion object {
        lateinit var bluetoothManager: BluetoothManager
    }
}
