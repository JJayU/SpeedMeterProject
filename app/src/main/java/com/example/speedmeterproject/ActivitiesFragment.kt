package com.example.speedmeterproject

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.FileProvider
import androidx.core.net.toUri
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

        binding.openFolderButton.setOnClickListener {
            // TODO
//            val dataDir = requireContext().filesDir
//            Log.i("a", "${requireContext().packageName}.fileprovider")
//            val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", dataDir)
//            val intent = Intent(Intent.ACTION_VIEW)
//            intent.setDataAndType(uri, "resource/folder")
//            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//            startActivity(intent)
        }

        // Inflate the layout for this fragment
        return binding.root
    }
}
