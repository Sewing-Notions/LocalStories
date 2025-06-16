package com.localstories

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.graphics.Typeface
import android.os.Build
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat
import com.localstories.viewmodel.StoriesViewModel
import kotlin.getValue

class ExploreActivity : AppCompatActivity() {
    private val storiesViewModel: StoriesViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StoryAdapter

    //private var receivedStories: List<Story> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore)

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            receivedStories = intent.getParcelableArrayListExtra("localStories", Story::class.java) ?: emptyList()
        } else {
            @Suppress("DEPRECATION")
            receivedStories = intent.getParcelableArrayListExtra<Story>("localStories") ?: emptyList()
        } */
        val closeBtn = findViewById<ImageButton>(R.id.closeExploreBtn)
        closeBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        recyclerView = findViewById(R.id.exploreRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this)


        val stories = mutableListOf(
            Story(
                storyId = "1",
                title = "The Hidden History of Central Square",
                description = "Discovering the fascinating story behind the city’s most iconic gathering place...",
                dateOfFact = "1889",
                photoPath = "", // or path to a placeholder
                locationId = "loc001",
                userId = "user001",
                author = "John Doe"
            ),
            Story(
                storyId = "2",
                title = "Victorian Architecture",
                description = "A beautiful example of 19th century design...",
                dateOfFact = "1895",
                photoPath = "",
                locationId = "loc002",
                userId = "user002",
                author = "Sarah Smith"
            ),
            Story(
                storyId = "3",
                title = "Market Square History",
                description = "Explore how this market transformed through decades...",
                dateOfFact = "1901",
                photoPath = "",
                locationId = "loc003",
                userId = "user003",
                author = "Alex Johnson"
            )
        )

        //adapter = StoryAdapter(stories, showDeleteButton = false)
        adapter = StoryAdapter(storiesViewModel.getStories()?.toMutableList() ?: stories.toMutableList(), showDeleteButton = false)
        recyclerView.adapter = adapter
        val featuredCard = findViewById<CardView>(R.id.featuredCard)
        featuredCard.setOnClickListener {
            val intent = Intent(this, StoryDetailActivity::class.java)
            intent.putExtra("storyId", "defaultFunStory")
            intent.putExtra("title", "The Mysterious Bell of Baker Street")
            intent.putExtra("description", "Every night at 9:03 PM, a forgotten bell chimes from beneath Baker Street. No one knows who rings it—or why. Locals say it's the ghost of a baker who accidentally created the world’s first garlic donut.")
            intent.putExtra("dateOfFact", "1897")
            intent.putExtra("locationId", "baker_st_ghost_bell")
            intent.putExtra("author", "Eleanor Finch")
            intent.putExtra("photoResId", R.drawable.bell) // or your placeholder image
            startActivity(intent)
        }

    }



}


