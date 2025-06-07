package com.localstories

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.localstories.R
import android.net.Uri
import android.widget.Button
import android.widget.Toast
import java.io.File
import android.widget.ImageButton
import com.localstories.viewmodel.MapViewModel

class StoryDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_detail)
        val mapViewModel = MapViewModel()

        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")
        val date = intent.getStringExtra("dateOfFact")
        val photoPath = intent.getStringExtra("photoPath")
        val location = intent.getStringExtra("locationId")

        findViewById<TextView>(R.id.storyTitle).text = title
        findViewById<TextView>(R.id.storyDescription).text = description
        findViewById<TextView>(R.id.storyDate).text = "Date: $date"
        findViewById<TextView>(R.id.storyLocation).text = "Location ID: $location"

        val imageView = findViewById<ImageView>(R.id.storyImage)

        if (!photoPath.isNullOrEmpty()) {
            val imgFile = File(photoPath)
            if (imgFile.exists()) {
                imageView.setImageURI(Uri.fromFile(imgFile))
            } else {
                imageView.setImageResource(R.drawable.image_rounded_bg)
            }
        } else {
            imageView.setImageResource(R.drawable.image_rounded_bg)
        }
        val closeBtn = findViewById<ImageButton>(R.id.closeButton)
        closeBtn.setOnClickListener {
            finish() // closes the current activity
        }
        val saveButton = findViewById<Button>(R.id.saveStoryButton)
        saveButton.setOnClickListener {
            val story = Story(
                storyId = intent.getStringExtra("storyId") ?: "",
                title = title ?: "",
                description = description ?: "",
                dateOfFact = date ?: "",
                photoPath = photoPath ?: "",
                locationId = location ?: "",
                userId = "", // Handle later after login
                author = intent.getStringExtra("author") ?: ""
            )

            SavedStories.stories.add(story)
            Toast.makeText(this, "Story saved!", Toast.LENGTH_SHORT).show()
        }
        val reportButton = findViewById<Button>(R.id.reportButton)
        reportButton.setOnClickListener {
            mapViewModel.addReport(intent.getStringExtra("storyId") ?: "")
            Toast.makeText(this, "Thank you. We’ll review this story.", Toast.LENGTH_SHORT).show()
        }
    }
}