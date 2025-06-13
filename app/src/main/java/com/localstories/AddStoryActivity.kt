package com.localstories

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.LatLng
import com.localstories.viewmodel.PinnedLocation

class AddStoryActivity : AppCompatActivity() {

    private lateinit var imagePlaceholder: ImageView
    private val PICK_IMAGE_REQUEST = 101
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_story)

        val locationNameEditText = findViewById<EditText>(R.id.locationName)
        val locationTimePeriodEditText = findViewById<EditText>(R.id.locationTimePeriod)
        val locationStoryEditText = findViewById<EditText>(R.id.locationStory)
        imagePlaceholder = findViewById(R.id.imagePreview)

        val closeButton = findViewById<ImageButton>(R.id.closeButton)
        closeButton.setOnClickListener {
            finish()
        }

        imagePlaceholder.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        val submitButton = findViewById<Button>(R.id.submitButton)
        submitButton.setOnClickListener {
            if (locationTimePeriodEditText.text.toString().isNotEmpty() ||
                locationStoryEditText.text.toString().isNotEmpty()) {

                val resultIntent = Intent()
                resultIntent.putExtra("storyTitle", locationNameEditText.text.toString())
                resultIntent.putExtra("storySnippet", locationStoryEditText.text.toString())
                resultIntent.putExtra("storyDate", locationTimePeriodEditText.text.toString())
                resultIntent.putExtra("imageUri", imageUri.toString()) // If you want to pass imageUri
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            imagePlaceholder.setImageURI(imageUri)
        }
    }

    fun formatLocation(locationName: String, locationInfo: String? = null): PinnedLocation {
        return PinnedLocation("70D0", LatLng(0.0, 0.0), locationName, locationInfo ?: null)
    }

    fun formatStory(storyTitle: String, storyDescription: String, storyDate: String): Story {
        return Story("70D0", storyTitle, storyDescription, storyDate, "/images/seattle_underground_ghost.jpg", "70D0", "70D0", "70D0")
    }
}