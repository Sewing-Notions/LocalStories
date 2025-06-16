package com.localstories

// for google maps functionality
import android.annotation.SuppressLint
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationRequest
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
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
import android.media.MediaPlayer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.localstories.viewmodel.StoriesViewModel
import com.localstories.viewmodel.StoryRepository
import com.localstories.viewmodel.UserLocationRepository
import kotlinx.coroutines.launch
//import okhttp3.Callback
import retrofit2.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>
    private val mapViewModel: MapViewModel by viewModels()
    private val storiesViewModel: StoriesViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                locationPermissionGranted = true
                getCurrentLocationAndFocus()
            } else {
                locationPermissionGranted = false
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private var locationPermissionGranted = false

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

        val ghost = findViewById<ImageView>(R.id.ghostGif)
        val booSound = MediaPlayer.create(this, R.raw.boo)

        ghost.setOnClickListener {
            booSound.start()
        }
        Glide.with(this)
            .asGif()
            .load(R.drawable.ghost)
            .into(ghost)

        val handler = Handler(Looper.getMainLooper())
        val ghostRunnable = object : Runnable {
            override fun run() {
                ghost.visibility = View.VISIBLE

                handler.postDelayed({
                    ghost.visibility = View.INVISIBLE
                    handler.postDelayed(this, 2 * 60 * 1000)  // show again in 2 minutes
                }, 4500)  // stays visible for 3 seconds
            }
        }

        handler.postDelayed(ghostRunnable, 5000)  // first show after 5 seconds
        val mapLayout = findViewById<ComposeView>(R.id.mapLayout)
        mapLayout.setContent {
            LocalStoriesTheme {
                //mapViewModel = MapViewModel()
                MapScreen(mapViewModel)
            }
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                StoryRepository.getStories().asFlow().collect { storiesFromRepository ->
                    val currentStories = storiesFromRepository ?: emptyList()
                    storiesViewModel.addStories(currentStories)
                    //Log.d("MainActivity", "Stories from repository: ${storiesViewModel.getStories()}")
                }
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

        val locationButton: View = findViewById(R.id.locationBtn)
        locationButton.setOnClickListener {
            checkLocationPermissionAndFocus()
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
                    val intent = Intent(this, ExploreActivity::class.java)
                    startActivity(intent)
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
        var localPlaces = emptyList<Place>()

        val adapter = PlaceAdapter(demoPlaces)
        nearbyList.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_STORY_REQUEST_CODE && resultCode == RESULT_OK) {
            val title = data?.getStringExtra("storyTitle") ?: "Default Title"
            val snippet = data?.getStringExtra("storySnippet") ?: "Default Snippet"
            val date = data?.getStringExtra("storyDate") ?: "Default Date"
            val imageUri = data?.getParcelableExtra<android.net.Uri>("imageUrl") ?: android.net.Uri.EMPTY
            val imageUrl = data?.getStringExtra("imageUrl") ?: ""

            //Log.d("MainActivity", "Pinned Location: $pinnedLocation")
            val pinnedLocation = formatPinnedLocation(title, date + "\n" + snippet)
            mapViewModel.addPinnedLocation(pinnedLocation, "35.247.54.23", "3000")
            if (imageUrl.isNotEmpty()) {
                mapViewModel.addStory(formatStory(title, snippet, date, pinnedLocation.id), "35.247.54.23", "3000")
            } else {
                uploadStoryWithImage(title, snippet, date, pinnedLocation.id, imageUri)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mapViewModel.saveCurrentCameraPositionToRepository()
    }

    fun formatPinnedLocation(title: String, info: String? = null): PinnedLocation {
        return mapViewModel.generatePinnedLocation(title, info)
    }
    fun formatStory(storyTitle: String, storyDescription: String, storyDate: String, locationId: String): Story {
        return mapViewModel.generateStory(storyTitle, storyDescription, storyDate, locationId)
    }

    fun uploadStoryWithImage(
        title: String,
        snippet: String,
        date: String,
        locationId: String,
        imageUri: android.net.Uri
    ) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                Log.e("Upload", "Failed to get InputStream from URI")
                Toast.makeText(this, "Error processing image.", Toast.LENGTH_SHORT).show()
                return
            }

            val requestFile = inputStream.readBytes().toRequestBody("image/jpeg".toMediaTypeOrNull())
            val fileName = imageUri.lastPathSegment ?: "image.jpg"
            val body = MultipartBody.Part.createFormData("photo", fileName, requestFile)
            val storyId = (UserLocationRepository.getLocation().value?.latitude.toString() +
                    UserLocationRepository.getLocation().value?.longitude.toString() + "-" +
                    SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()).toString())

            val storyIdRequestBody = storyId.toRequestBody("text/plain".toMediaTypeOrNull())
            val titleRequestBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val snippetRequestBody = snippet.toRequestBody("text/plain".toMediaTypeOrNull())
            val dateRequestBody = date.toRequestBody("text/plain".toMediaTypeOrNull())
            val locationIdRequestBody = locationId.toRequestBody("text/plain".toMediaTypeOrNull())
            val userIdRequestBody = "789".toRequestBody("text/plain".toMediaTypeOrNull()) // Replace with actual user ID
            val retrofit = Retrofit.Builder()
                .baseUrl("http://35.247.54.23:3000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(ApiService::class.java)
            val call = service.uploadStory(
                body,
                storyIdRequestBody,
                titleRequestBody,
                snippetRequestBody,
                dateRequestBody,
                locationIdRequestBody,
                userIdRequestBody
            )

            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Log.d("Upload", "Story with image uploaded successfully!")
                        Toast.makeText(this@MainActivity, "Story uploaded!", Toast.LENGTH_SHORT).show()
                        Log.d("Upload", "Response: ${response.body()?.string()}")
                    } else {
                        Log.e("Upload", "Failed to upload story: ${response.code()} - ${response.errorBody()?.string()}")
                        Toast.makeText(this@MainActivity, "Upload failed: ${response.message()}", Toast.LENGTH_LONG).show()
                    }
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("Upload", "Error uploading story", t)
                    Toast.makeText(this@MainActivity, "Upload error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })

        } catch (e: Exception) {
            Log.e("Upload", "Exception during image processing or upload", e)
            Toast.makeText(this, "An error occurred: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkLocationPermissionAndFocus() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                locationPermissionGranted = true
                getCurrentLocationAndFocus()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(this, "Location permission is needed to show current location.", Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocationAndFocus() {
        if (locationPermissionGranted) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        mapViewModel.moveToLocation(currentLatLng)
                    } else {
                        Toast.makeText(this, "Unable to get current location. Make sure location is enabled.", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to get current location.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Location permission not granted.", Toast.LENGTH_SHORT).show()
        }
    }

}