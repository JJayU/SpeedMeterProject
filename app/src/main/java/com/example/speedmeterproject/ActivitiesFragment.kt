package com.example.speedmeterproject

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.speedmeterproject.databinding.FragmentActivitiesBinding
import kotlinx.coroutines.launch


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

        binding.openFolderButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            val uri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).path + "/SpeedMeterApp/")
            intent.setDataAndType(uri, "*/*")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            startActivity(intent)
        }

        // Inflate the layout for this fragment
        return binding.root
    }
}
