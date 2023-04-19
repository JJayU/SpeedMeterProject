package com.example.speedmeterproject

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.speedmeterproject.databinding.FragmentActivitiesBinding
import com.example.speedmeterproject.databinding.FragmentMainBinding
import org.jetbrains.annotations.TestOnly


class ActivitiesFragment : Fragment() {

    private lateinit var binding : FragmentActivitiesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_activities, container, false)

        val adapter = ActivitiesListAdapter(createList())
        binding.recyclerView.layoutManager = LinearLayoutManager(super.getContext())
        binding.recyclerView.adapter = adapter

        // Inflate the layout for this fragment
        return binding.root
    }

    //TODO -> remove
    private fun createList() : List<ActivityItem> = buildList {
        for (i in 0..20) {
            val newActivity = ActivityItem("Activity Name $i", "$i", "$i", "$i")
            add(newActivity)
        }
    }

}