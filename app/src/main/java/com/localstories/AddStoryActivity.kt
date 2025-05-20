
package com.localstories

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton
class AddStoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_story)
        val closeButton = findViewById<ImageButton>(R.id.closeButton)
        closeButton.setOnClickListener {
            finish() // This closes the current activity and returns to MainActivity
        }
    }
}