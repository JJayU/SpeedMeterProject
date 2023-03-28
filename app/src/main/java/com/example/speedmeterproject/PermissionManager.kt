package com.example.speedmeterproject

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat

class PermissionManager {

    fun onStartupCheck(context : Context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
            Toast.makeText(context, "No permission for Bluetooth or location!", Toast.LENGTH_LONG).show()
        }
    }

    fun hasBluetoothPermission(context: Context) : Boolean {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return true
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray, context: Context) {
        when (requestCode) {
            1 -> {
                // Check if user has given permissions
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // Permissions granted
                    Toast.makeText(context, "Przyznano uprawienia", Toast.LENGTH_LONG).show()
                } else {
                    // Permissions not granted
                    Toast.makeText(context, "Permissions not granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}

