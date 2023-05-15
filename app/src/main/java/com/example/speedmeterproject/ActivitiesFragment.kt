package com.example.speedmeterproject

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.speedmeterproject.databinding.FragmentActivitiesBinding
import kotlinx.coroutines.launch
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists


class ActivitiesFragment : Fragment() {

    private lateinit var binding : FragmentActivitiesBinding
    private lateinit var repo : Repository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_activities, container, false)
        val fragmentContext : Context = super.requireContext()
        repo = Repository(inflater.context)

        viewLifecycleOwner.lifecycleScope.launch {
            var activitiesList : List<DbActivityItem> = repo.getAll().reversed()
            // Show "no activities" when there are no activities
            if(activitiesList.isEmpty()) {
                binding.noActivitySaved.visibility = View.VISIBLE
            }

            // Inflate activities list
            val adapter = ActivitiesListAdapter(activitiesList)
            binding.recyclerView.layoutManager = LinearLayoutManager(super.getContext())
            binding.recyclerView.adapter = adapter

            // Show remove activity dialog
            adapter.setOnClickListener(object :
                ActivitiesListAdapter.OnClickListener {
                override fun onClick(position: Int, model: DbActivityItem) {
                    val alertDialogBuilder = AlertDialog.Builder(fragmentContext)
                    alertDialogBuilder.setTitle(getString(R.string.are_you_sure_you_want_to_delete_this_activity))
                    alertDialogBuilder.setMessage(R.string.cannot_undo)
                    alertDialogBuilder.setPositiveButton(R.string.yes) { _, _ ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            repo.delete(listOf<DbActivityItem>(DbActivityItem(model.uid, model.name, model.distance, model.time, model.avgSpeed, model.date)))
                        }

                        val uri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).path + "/SpeedMeterApp/" + model.name + ".tcx")
                        val path = Path(uri.toString())
                        path.deleteIfExists()

                        Toast.makeText(fragmentContext, getString(R.string.activity_removed), Toast.LENGTH_LONG).show()

                        val navController = Navigation.findNavController(binding.root)
                        navController.navigate(R.id.action_activitiesFragment_self)
                    }
                    alertDialogBuilder.setNegativeButton(R.string.no) { _, _ -> } // Do nothing
                    alertDialogBuilder.show()
                }
            })
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
