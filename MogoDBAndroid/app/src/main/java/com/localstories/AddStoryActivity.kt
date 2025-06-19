
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
            if (!locationTimePeriodEditText.text.toString().isEmpty() || !locationStoryEditText.text.toString().isEmpty()) {
                // TODO update this to check if the location is already in the database
                var locationData = locationTimePeriodEditText.text.toString() +
                        "\n" + locationStoryEditText.text.toString()
                val userLocation = mapViewModel.generatePinnedLocation(locationNameEditText.text.toString(), locationData)
                mapViewModel.addPinnedLocation(userLocation, "35.247.54.23", "3000")

                var locationStory: Story = formatStory(locationNameEditText.text.toString(),
                    locationStoryEditText.text.toString(),
                    locationTimePeriodEditText.text.toString())
                mapViewModel.addStory(locationStory, "35.247.54.23", "3000")
            }
        }
    }

    fun formatStory(storyTitle: String, storyDescription: String, storyDate: String): Story {
        return Story("70D0", storyTitle, storyDescription, storyDate, "", "70D0", "70D0", "70D0")
    }
}