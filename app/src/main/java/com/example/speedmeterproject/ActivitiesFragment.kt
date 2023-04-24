package com.example.speedmeterproject

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.speedmeterproject.databinding.FragmentActivitiesBinding
import com.example.speedmeterproject.databinding.FragmentMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import org.jetbrains.annotations.TestOnly


class ActivitiesFragment : Fragment() {

    private lateinit var binding : FragmentActivitiesBinding
    private lateinit var repo : Repository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_activities, container, false)
        repo = Repository(inflater.context)

        viewLifecycleOwner.lifecycleScope.launch {
            val activitiesList : List<DbActivityItem> = repo.getAll().reversed()
            // Show "no activities" when there are no activities
            if(activitiesList.isEmpty()) {
                binding.noActivitySaved.visibility = View.VISIBLE
            }
            val adapter = ActivitiesListAdapter(activitiesList)
            binding.recyclerView.layoutManager = LinearLayoutManager(super.getContext())
            binding.recyclerView.adapter = adapter
        }

        // Inflate the layout for this fragment
        return binding.root
    }
}
