package com.localstories
import com.bumptech.glide.Glide
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton
import android.widget.Button
import android.widget.EditText
import com.google.android.gms.maps.model.LatLng
import com.localstories.viewmodel.PinnedLocation
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.floatingactionbutton.FloatingActionButton

object  StorySession {
    var storyId: String = ""
    var title: String = ""
    var description: String = ""
    var dateOfFact: String = ""
    var locationId: String = ""
    var userId: String = ""
    var imageUrl: String = ""
    var imageUri: Uri? = null
}

class AddStoryActivity : AppCompatActivity() {
    private lateinit var imagePlaceholder: ImageView
    val startCameraActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri = result.data?.getParcelableExtra<android.net.Uri>("imageUri")
            imagePlaceholder.setImageURI(imageUri)
            StorySession.imageUri = imageUri
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_story)
        val ghostGif = findViewById<ImageView>(R.id.ghostGif)

// 1. Make it visible
        ghostGif.visibility = ImageView.VISIBLE

// 2. Load GIF with Glide
        Glide.with(this)
            .asGif()
            .load(R.drawable.ghost2)
            .into(ghostGif)

// 3. Hide after 3 seconds with fade-out animation
        ghostGif.postDelayed({
            ghostGif.animate()
                .alpha(0f)
                .setDuration(500)
                .withEndAction {
                    ghostGif.visibility = ImageView.GONE
                    ghostGif.alpha = 1f // Reset alpha
                }
                .start()
        }, 3000)

        // UI Components
        val imageUrlInput = findViewById<EditText>(R.id.imageUrlInput)
        imagePlaceholder = findViewById(R.id.imagePreview)
        val locationNameEditText = findViewById<EditText>(R.id.locationName)
        val locationTimePeriodEditText = findViewById<EditText>(R.id.locationTimePeriod)
        val locationStoryEditText = findViewById<EditText>(R.id.locationStory)


        val cameraButton = findViewById<FloatingActionButton>(R.id.pickImageIcon)
        cameraButton.setOnClickListener {
            // Start CameraActivity().checkPermissionsAndOpenCamera()
            val intent = Intent(this, CameraActivity::class.java)
            startCameraActivity.launch(intent)
        }

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
            // upload image to server and get image URL

            if (title.isNotEmpty() &&
                snippet.isNotEmpty() &&
                date.isNotEmpty()) {
                val resultIntent = Intent()
                resultIntent.putExtra("storyTitle", title)
                resultIntent.putExtra("storySnippet", snippet)
                resultIntent.putExtra("storyDate", date)
                if (StorySession.imageUri != null) resultIntent.putExtra("imageUri", StorySession.imageUri)
                else if (imageUrlInput.text.toString().trim().isNotEmpty()) resultIntent.putExtra("imageUrl", imageUrlInput.text.toString().trim())
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