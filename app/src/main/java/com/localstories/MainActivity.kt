package com.localstories

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.localstories.databinding.ActivityMainBinding

// for google maps functionality
import com.localstories.ui.theme.LocalStoriesTheme
import com.google.android.libraries.places.api.Places
import com.localstories.screens.MapScreen
import com.localstories.utils.ManifestUtils
import com.localstories.viewmodel.MapViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val apiKey = ManifestUtils.getApiKeyFromManifest(this)

        if (!Places.isInitialized() && apiKey != null) {
            Places.initialize(applicationContext, apiKey)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapLayout = findViewById<ComposeView>(R.id.mapLayout)
        mapLayout.setContent {
            LocalStoriesTheme {
                val mapViewModel = MapViewModel()
                MapScreen(mapViewModel)
            }
        }

        drawerLayout = findViewById(R.id.drawerLayout)

        val menuButton: MaterialButton = findViewById(R.id.menuButton)
        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        val compassButton: Button = findViewById(R.id.compassButton)
        compassButton.setOnClickListener {
            Toast.makeText(this, "Compass Clicked!", Toast.LENGTH_SHORT).show()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_add -> {
                    startActivity(Intent(this, AddStoryActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Setup BottomSheetBehavior
        try {
            val bottomSheet = findViewById<FrameLayout>(R.id.bottomSheetContainer)
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior.peekHeight = 650
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

            // Hide/show FABs based on sheet state
            bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    val fabGroup = findViewById<LinearLayout>(R.id.fabGroup)
                    fabGroup.visibility = if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    val fabGroup = findViewById<LinearLayout>(R.id.fabGroup)
                    fabGroup.alpha = 1 - slideOffset.coerceIn(0f, 1f)
                    fabGroup.visibility = if (slideOffset >= 0.2f) View.GONE else View.VISIBLE
                }
            })

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "BottomSheet error: ${e.message}", Toast.LENGTH_LONG).show()
        }

        // RecyclerView
        val nearbyList = findViewById<RecyclerView>(R.id.nearbyList)
        nearbyList.layoutManager = LinearLayoutManager(this)

        val demoPlaces = listOf(
            Place("Old Town Hall", "0.3 km away • Built in 1892"),
            Place("Heritage Library", "0.6 km away • Built in 1901"),
            Place("City Clock Tower", "0.8 km away • Built in 1850")
        )

        val adapter = PlaceAdapter(demoPlaces)
        nearbyList.adapter = adapter
    }
}