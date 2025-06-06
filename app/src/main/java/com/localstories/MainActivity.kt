package com.localstories

// for google maps functionality
import android.content.Intent
import android.location.LocationRequest
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.localstories.databinding.ActivityMainBinding
import com.localstories.screens.MapScreen
import com.localstories.ui.theme.LocalStoriesTheme
import com.localstories.utils.ManifestUtils
import com.localstories.viewmodel.MapViewModel
import com.localstories.viewmodel.PinnedLocation

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>
    private lateinit var mapViewModel: MapViewModel

    companion object {
        const val ADD_STORY_REQUEST_CODE = 101
    }

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
                mapViewModel = MapViewModel()
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
            val intent = Intent(this, CompassActivity::class.java)
            startActivity(intent)
            drawerLayout.closeDrawer(GravityCompat.START)
        }


        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_add -> {
                    val intent = Intent(this, AddStoryActivity::class.java)
                    startActivityForResult(intent, ADD_STORY_REQUEST_CODE)
                    //startActivity(Intent(this, AddStoryActivity::class.java))
                    true
                }
                R.id.nav_explore -> {
                    startActivity(Intent(this, ExploreActivity::class.java))
                    true
                }
                R.id.nav_saved -> {
                    startActivity(Intent(this, SavedActivity::class.java))
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_STORY_REQUEST_CODE && resultCode == RESULT_OK) {
            val title = data?.getStringExtra("storyTitle") ?: "Default Title"
            val snippet = data?.getStringExtra("storySnippet") ?: "Default Snippet"
            val date = data?.getStringExtra("storyDate") ?: "Default Date"

            //Log.d("MainActivity", "Pinned Location: $pinnedLocation")
            val pinnedLocation = formatPinnedLocation(title, date + "\n" + snippet)
            mapViewModel.addPinnedLocation(pinnedLocation, "35.247.54.23", "3000")
            mapViewModel.addStory(formatStory(title, snippet, date, pinnedLocation.id), "35.247.54.23", "3000")
        }
    }

    fun formatPinnedLocation(title: String, info: String? = null): PinnedLocation {
        return mapViewModel.generatePinnedLocation(title, info)
    }
    fun formatStory(storyTitle: String, storyDescription: String, storyDate: String, locationId: String): Story {
        return mapViewModel.generateStory(storyTitle, storyDescription, storyDate, locationId)
    }
}