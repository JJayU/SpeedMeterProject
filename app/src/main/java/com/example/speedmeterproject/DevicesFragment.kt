package com.example.speedmeterproject

import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.speedmeterproject.databinding.FragmentActivitiesBinding
import com.example.speedmeterproject.databinding.FragmentDevicesBinding
import com.example.speedmeterproject.databinding.FragmentMainBinding
import kotlinx.coroutines.launch

class DevicesFragment : Fragment() {

    private lateinit var binding : FragmentDevicesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_devices, container, false)

        viewLifecycleOwner.lifecycleScope.launch {
            val devicesList = BluetoothBridge.bluetoothManager.pairedDevicesList
            // Show "no devices paired" when there are no paired devices
            if(devicesList.isEmpty()) {
                binding.noDevicesPaired.visibility = View.VISIBLE
            }
            val adapter = DevicesListAdapter(devicesList)
            binding.recyclerView.layoutManager = LinearLayoutManager(super.getContext())
            binding.recyclerView.adapter = adapter
        }

        binding.pairButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
        }

        // Inflate the layout for this fragment
        return binding.root
    }
}