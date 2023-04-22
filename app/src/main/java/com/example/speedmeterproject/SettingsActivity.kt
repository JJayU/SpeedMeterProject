package com.example.speedmeterproject

import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val circPref : EditTextPreference? = findPreference("wheel_circ")

            circPref?.setOnBindEditTextListener {
                editText -> editText.inputType = InputType.TYPE_CLASS_NUMBER
            }

            val removeAllButton : Preference? = findPreference("delete_activities")
            removeAllButton?.setOnPreferenceClickListener {

                val alertDialogBuilder = AlertDialog.Builder(this.requireContext())
                alertDialogBuilder.setTitle("Are you sure?")
                alertDialogBuilder.setMessage("This action cannot be undone")
                alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
                    val repo = Repository(this.requireContext())

                    viewLifecycleOwner.lifecycleScope.launch {
                        repo.deleteAll()
                    }
                }
                alertDialogBuilder.setNegativeButton("No") { _, _ ->

                }
                alertDialogBuilder.show()

                true
            }

        }
    }
}