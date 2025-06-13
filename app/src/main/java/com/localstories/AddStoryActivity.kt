package com.localstories
import com.bumptech.glide.Glide
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton
import android.widget.Button
import android.widget.EditText
import com.google.android.gms.maps.model.LatLng
import com.localstories.viewmodel.PinnedLocation
import android.widget.ImageView


class AddStoryActivity : AppCompatActivity() {
    private lateinit var imagePlaceholder: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_story)

        // UI Components
        val imageUrlInput = findViewById<EditText>(R.id.imageUrlInput)
        imagePlaceholder = findViewById(R.id.imagePreview)
        val locationNameEditText = findViewById<EditText>(R.id.locationName)
        val locationTimePeriodEditText = findViewById<EditText>(R.id.locationTimePeriod)
        val locationStoryEditText = findViewById<EditText>(R.id.locationStory)
        val closeButton = findViewById<ImageButton>(R.id.closeButton)
        val submitButton = findViewById<Button>(R.id.submitButton)

        // Load image from pasted URL when focus is lost
        imageUrlInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val url = imageUrlInput.text.toString().trim()
                if (url.isNotEmpty()) {
                    Glide.with(this)
                        .load(url)
                        .placeholder(R.drawable.bg_rounded_edittext)
                        .error(android.R.drawable.ic_dialog_alert)
                        .into(imagePlaceholder)
                }
            }
        }

        // Close button
        closeButton.setOnClickListener {
            finish()
        }

        // Submit button
        submitButton.setOnClickListener {
            val title = locationNameEditText.text.toString()
            val snippet = locationStoryEditText.text.toString()
            val date = locationTimePeriodEditText.text.toString()

            if (snippet.isNotEmpty() || date.isNotEmpty()) {
                val resultIntent = Intent()
                resultIntent.putExtra("storyTitle", title)
                resultIntent.putExtra("storySnippet", snippet)
                resultIntent.putExtra("storyDate", date)
                resultIntent.putExtra("imageUrl", imageUrlInput.text.toString().trim()) // Pass image URL
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