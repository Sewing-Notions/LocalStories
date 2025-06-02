
package com.localstories

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton
import android.widget.Button
import android.widget.EditText
import com.localstories.viewmodel.MapViewModel

class AddStoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_story)

        val mapViewModel = MapViewModel() // Initialize the MapViewModel
        val locationNameEditText = findViewById<EditText>(R.id.locationName)
        val locationTimePeriodEditText = findViewById<EditText>(R.id.locationTimePeriod)
        val locationStoryEditText = findViewById<EditText>(R.id.locationStory)

        val closeButton = findViewById<ImageButton>(R.id.closeButton)
        closeButton.setOnClickListener {
            finish() // This closes the current activity and returns to MainActivity
        }

        val submitButton = findViewById<Button>(R.id.submitButton)
        submitButton.setOnClickListener {
            var locationStory = ""
            if (!locationTimePeriodEditText.text.toString().isEmpty() || !locationStoryEditText.text.toString().isEmpty()) {
                locationStory = locationTimePeriodEditText.text.toString() +
                        "\n" + locationStoryEditText.text.toString()
            }

            val userLocation = mapViewModel.generatePinnedLocation(locationNameEditText.text.toString(), locationStory)
            mapViewModel.addPinnedLocation(userLocation, "35.247.54.23", "3000")
        }
    }
}