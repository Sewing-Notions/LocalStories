package com.localstories

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SavedActivity : AppCompatActivity() {
    private lateinit var adapter: StoryAdapter
    private lateinit var savedStories: MutableList<Story>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved)

        val closeButton = findViewById<ImageButton>(R.id.closeButton)
        closeButton.setOnClickListener {
            finish()
        }

        savedStories = mutableListOf(
            Story("Victorian Architecture", "Sarah Smith", "A beautiful example of 19th century design..."),
            Story("Old Harbor Mystery", "Anna Lee", "The untold story of a historic port town...")
        )

        val recyclerView = findViewById<RecyclerView>(R.id.savedRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = StoryAdapter(savedStories, showDeleteButton = true) { position ->
            savedStories.removeAt(position)
            adapter.notifyItemRemoved(position)
        }

        recyclerView.adapter = adapter
    }
}