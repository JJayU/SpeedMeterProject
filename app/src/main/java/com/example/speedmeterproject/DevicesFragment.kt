package com.example.speedmeterproject

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.speedmeterproject.databinding.FragmentDevicesBinding
import kotlinx.coroutines.launch

class DevicesFragment : Fragment() {

    private lateinit var binding : FragmentDevicesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_devices, container, false)
        val fragmentContext : Context = super.requireContext()

        viewLifecycleOwner.lifecycleScope.launch {
            // Get list of paired devices
            val devicesList = BluetoothBridge.bluetoothManager.pairedDevicesList

            // Show "no devices paired" when there are no paired devices
            if(devicesList.isEmpty()) {
                binding.noDevicesPaired.visibility = View.VISIBLE
            }

            // Inflate devices list
            val adapter = DevicesListAdapter(devicesList)
            binding.recyclerView.layoutManager = LinearLayoutManager(fragmentContext)
            binding.recyclerView.adapter = adapter

            // Set destination device address to clicked one
            adapter.setOnClickListener( object :
                DevicesListAdapter.OnClickListener {
                @SuppressLint("MissingPermission")
                override fun onClick(position: Int, model: BluetoothDevice) {
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(fragmentContext)
                    sharedPreferences.edit().putString("bike_mac_address", model.address).apply()
                    Toast.makeText(fragmentContext, getString(R.string.device_selected, model.name), Toast.LENGTH_LONG).show()
                }
            })

        }

        // Launch system bluetooth settings on pairButton press
        binding.pairButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
        }

        // Inflate the layout for this fragment
        return binding.root
    }
}