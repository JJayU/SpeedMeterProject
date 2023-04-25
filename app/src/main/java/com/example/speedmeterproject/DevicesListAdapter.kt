package com.example.speedmeterproject

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.speedmeterproject.databinding.ActivityRowBinding
import com.example.speedmeterproject.databinding.DeviceRowBinding

class DevicesListAdapter(private val devices : List<BluetoothDevice>) : RecyclerView.Adapter<DevicesListAdapter.DevicesViewHolder>() {

    inner class DevicesViewHolder(binding : DeviceRowBinding) : ViewHolder(binding.root) {
        val nameTv = binding.deviceName
        val addressTv = binding.deviceAddress
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevicesViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val deviceRowBinding = DeviceRowBinding.inflate(inflater, parent, false)
        return DevicesViewHolder(deviceRowBinding)
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: DevicesViewHolder, position: Int) {
        holder.nameTv.text = devices[position].name
        holder.addressTv.text = devices[position].address
    }

}