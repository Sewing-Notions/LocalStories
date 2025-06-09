package com.localstories

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.graphics.Typeface
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat
class ExploreActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StoryAdapter

    private var receivedStories: List<Story> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            receivedStories = intent.getParcelableArrayListExtra("localStories", Story::class.java) ?: emptyList()
        } else {
            @Suppress("DEPRECATION")
            receivedStories = intent.getParcelableArrayListExtra<Story>("localStories") ?: emptyList()
        }
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
        adapter = StoryAdapter(receivedStories.toMutableList(), showDeleteButton = false)
        recyclerView.adapter = adapter

        setupFilterButtons()
    }

    private fun setupFilterButtons() {
        val btnAll = findViewById<Button>(R.id.btnAllPlaces)
        val btnBuildings = findViewById<Button>(R.id.btnBuildings)
        val btnMonuments = findViewById<Button>(R.id.btnMonuments)

        val buttons = listOf(btnAll, btnBuildings, btnMonuments)

        fun setSelected(button: Button) {
            buttons.forEach { btn ->
                btn.setBackgroundResource(R.drawable.filter_button_unselected)
                btn.setTextColor(ContextCompat.getColor(this, android.R.color.black))
                btn.setTypeface(null, Typeface.NORMAL)
            }

            button.setBackgroundResource(R.drawable.filter_button_selected)
            button.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            button.setTypeface(null, Typeface.BOLD)
        }

        btnAll.setOnClickListener { setSelected(btnAll) }
        btnBuildings.setOnClickListener { setSelected(btnBuildings) }
        btnMonuments.setOnClickListener { setSelected(btnMonuments) }

        // Select "All Places" by default
        setSelected(btnAll)
    }
    private fun updateFilterButtonStyles(selectedButtonId: Int) {
        val allButtons = listOf(
            findViewById<Button>(R.id.btnAllPlaces),
            findViewById<Button>(R.id.btnBuildings),
            findViewById<Button>(R.id.btnMonuments)
        )

        allButtons.forEach { button ->
            if (button.id == selectedButtonId) {
                // Selected style
                button.setBackgroundResource(R.drawable.filter_button_selected)
                button.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            } else {
                // Unselected style
                button.setBackgroundResource(R.drawable.filter_button_unselected)
                button.setTextColor(ContextCompat.getColor(this, android.R.color.black))
            }
        }
    }
}


