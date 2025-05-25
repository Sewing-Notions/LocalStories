package com.localstories

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat
class ExploreActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore)

        val closeBtn = findViewById<ImageButton>(R.id.closeExploreBtn)
        closeBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        recyclerView = findViewById(R.id.exploreRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val stories = mutableListOf(
            Story("The Hidden History of Central Square", "John Doe", "Discovering the fascinating story behind the city’s most iconic gathering place..."),
            Story("Victorian Architecture", "Sarah Smith", "A beautiful example of 19th century design..."),
            Story("Market Square History", "Alex Johnson", "Explore how this market transformed through decades...")
        )

        adapter = StoryAdapter(stories, showDeleteButton = false)
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


