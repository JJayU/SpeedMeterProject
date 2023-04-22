package com.example.speedmeterproject

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.speedmeterproject.databinding.ActivityRowBinding

class ActivitiesListAdapter(private val activities : List<DbActivityItem>) : RecyclerView.Adapter<ActivitiesListAdapter.ActivitiesViewHolder>() {

    inner class ActivitiesViewHolder(binding : ActivityRowBinding) : ViewHolder(binding.root) {
        val nameTv = binding.activityName
        val distanceTv = binding.activityTripDistance
        val timeTv = binding.activityTripTime
        val avgSpeedTv = binding.activityAvgSpeed
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivitiesViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val activityRowBinding = ActivityRowBinding.inflate(inflater, parent, false)
        return ActivitiesViewHolder(activityRowBinding)
    }

    override fun getItemCount(): Int {
        return activities.size
    }

    override fun onBindViewHolder(holder: ActivitiesViewHolder, position: Int) {
        holder.nameTv.text = activities[position].name
        holder.distanceTv.text = activities[position].distance
        holder.avgSpeedTv.text = activities[position].avgSpeed
        holder.timeTv.text = activities[position].time
    }

}