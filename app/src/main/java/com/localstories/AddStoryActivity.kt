
package com.localstories

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton
import android.widget.Button
import android.widget.EditText
import com.google.android.gms.maps.model.LatLng
import com.localstories.viewmodel.PinnedLocation


class AddStoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_story)

        //mapViewModel = ViewModelProvider(this)[MapViewModel::class.java]

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
                /*var locationData = locationTimePeriodEditText.text.toString() +
                        "\n" + locationStoryEditText.text.toString()
                //val userLocation = formatLocation(locationNameEditText.text.toString(), locationData)
                var locationStory: Story = formatStory(locationNameEditText.text.toString(),
                    locationStoryEditText.text.toString(),
                    locationTimePeriodEditText.text.toString())
                 */
                val resultIntent = Intent()
                resultIntent.putExtra("storyTitle", locationNameEditText.text.toString())
                resultIntent.putExtra("storySnippet", locationStoryEditText.text.toString())
                resultIntent.putExtra("storyDate", locationTimePeriodEditText.text.toString())
                setResult(RESULT_OK, resultIntent)

                finish()
            }
        }
    }

    fun formatLocation(locationName: String, locationInfo: String? = null): PinnedLocation {
        return PinnedLocation("70D0", LatLng(0.0, 0.0), locationName, locationInfo?: null)
    }
    fun formatStory(storyTitle: String, storyDescription: String, storyDate: String): Story {
        return Story("70D0", storyTitle, storyDescription, storyDate, "/images/seattle_underground_ghost.jpg", "70D0", "70D0", "70D0")
    }
}