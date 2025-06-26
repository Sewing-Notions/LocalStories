package com.localstories

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.localstories.R
//import android.net.Uri
import android.util.Log
import android.widget.Button
import android.widget.Toast
import java.io.File
import android.widget.ImageButton
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.localstories.utils.ManifestUtils
import com.localstories.viewmodel.MapViewModel
import com.localstories.viewmodel.StoriesViewModel
import kotlin.getValue

class StoryDetailActivity : AppCompatActivity() {
    private val storiesViewModel: StoriesViewModel by viewModels()

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

        val photoResId = intent.getIntExtra("photoResId", -1)


        if (photoResId != -1) {
            imageView.setImageResource(photoResId)
        } else if (!photoPath.isNullOrEmpty()) {
            Glide.with(this)
                .load(((ManifestUtils.getDbUrlFromManifest(this) + "/") ?: "http://xlynseyes.ddns.net:3000/") + photoPath)
                .placeholder(R.drawable.image_rounded_bg)
                .error(R.drawable.image_rounded_bg)
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.image_rounded_bg)
        }


        val closeBtn = findViewById<ImageButton>(R.id.closeExploreBtn)
        closeBtn.setOnClickListener {
            val intent = Intent(this, ExploreActivity::class.java)
            //intent.putParcelableArrayListExtra("localStories", storiesViewModel.stories.value as ArrayList<Story>)
            startActivity(intent)
            finish()
        }
        val saveButton = findViewById<Button>(R.id.saveStoryButton)
        saveButton.setOnClickListener {
            val storyId = intent.getStringExtra("storyId") ?: ""  // ✅ move this to the top!


            if (storyId == "defaultFunStory") {
                Toast.makeText(this, "You can't save the default story.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


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

            if (!SavedStories.stories.any { it.storyId == story.storyId }) {
                SavedStories.stories.add(story)
                Toast.makeText(this, "Story saved!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Story already saved.", Toast.LENGTH_SHORT).show()
            }
        }
        val reportButton = findViewById<Button>(R.id.reportButton)
        reportButton.setOnClickListener {
            mapViewModel.addReport(intent.getStringExtra("storyId") ?: "", ManifestUtils.getDbUrlFromManifest(this) ?: "http://xlynseyes.ddns.net:3000/")
            Toast.makeText(this, "Thank you. We’ll review this story.", Toast.LENGTH_SHORT).show()
        }
    }
}