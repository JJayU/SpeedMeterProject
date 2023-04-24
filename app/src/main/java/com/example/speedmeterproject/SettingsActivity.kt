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

    // Navigate back when user clicks back button in top bar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return super.onSupportNavigateUp()
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val circPref : EditTextPreference? = findPreference("wheel_circ")

            // Define numbers only edit text
            circPref?.setOnBindEditTextListener {
                editText -> editText.inputType = InputType.TYPE_CLASS_NUMBER
            }

            // Add a dialog asking if user is sure about clearing DB
            val removeAllButton : Preference? = findPreference("delete_activities")
            removeAllButton?.setOnPreferenceClickListener {

                // Build dialog asking if user is sure about clearing DB
                val alertDialogBuilder = AlertDialog.Builder(this.requireContext())
                alertDialogBuilder.setTitle(R.string.are_you_sure)
                alertDialogBuilder.setMessage(R.string.cannot_undo)
                alertDialogBuilder.setPositiveButton(R.string.yes) { _, _ ->
                    val repo = Repository(this.requireContext())

                    viewLifecycleOwner.lifecycleScope.launch {
                        repo.deleteAll()
                    }
                }
                alertDialogBuilder.setNegativeButton(R.string.no) { _, _ -> } // Do nothing
                alertDialogBuilder.show()

                //TODO -> remove files?

                true
            }
        }
    }
}
