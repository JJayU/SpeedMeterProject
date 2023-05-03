package com.example.speedmeterproject

import android.os.Bundle
import android.os.Environment
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
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
import java.io.File

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
                editText.filters = arrayOf<InputFilter>(MinMaxFilter(0, 5000))
            }

            // Add a dialog asking if user is sure about clearing DB
            val removeAllButton : Preference? = findPreference("delete_activities")
            removeAllButton?.setOnPreferenceClickListener {

                // Build dialog asking if user is sure about clearing DB
                val alertDialogBuilder = AlertDialog.Builder(this.requireContext())
                alertDialogBuilder.setTitle(R.string.are_you_sure)
                alertDialogBuilder.setMessage(R.string.cannot_undo)
                alertDialogBuilder.setPositiveButton(R.string.yes) { _, _ ->
                    // Remove database content
                    val repo = Repository(this.requireContext())
                    viewLifecycleOwner.lifecycleScope.launch {
                        repo.deleteAll()
                    }

                    // Remove files from storage
                    val docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    val appDir = File(docsDir, "SpeedMeterApp")
                    for (file in appDir.listFiles()) {
                        file.delete()
                    }
                }
                alertDialogBuilder.setNegativeButton(R.string.no) { _, _ -> } // Do nothing
                alertDialogBuilder.show()

                true
            }
        }

        // Custom class to define min and max for the edit text
        // From: https://www.geeksforgeeks.org/how-to-set-minimum-and-maximum-input-value-in-edittext-in-android/
        inner class MinMaxFilter() : InputFilter {
            private var intMin: Int = 0
            private var intMax: Int = 0

            // Initialized
            constructor(minValue: Int, maxValue: Int) : this() {
                this.intMin = minValue
                this.intMax = maxValue
            }

            override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dStart: Int, dEnd: Int): CharSequence? {
                try {
                    val input = Integer.parseInt(dest.toString() + source.toString())
                    if (isInRange(intMin, intMax, input)) {
                        return null
                    }
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }
                return ""
            }

            // Check if input c is in between min a and max b and
            // returns corresponding boolean
            private fun isInRange(a: Int, b: Int, c: Int): Boolean {
                return if (b > a) c in a..b else c in b..a
            }
        }
    }
}
