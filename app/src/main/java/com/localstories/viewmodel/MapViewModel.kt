package com.localstories.viewmodel

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.kotlin.localDate
import com.localstories.Story
import com.localstories.utils.ManifestUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient

import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.util.Date
import kotlin.getValue

object LocationRepository {
    private val locationRepositoryLiveData = MutableLiveData<List<PinnedLocation>>(emptyList())
    val pinnedLocations: LiveData<List<PinnedLocation>> = locationRepositoryLiveData

    val pinnedLocationsFlow: Flow<List<PinnedLocation>> = pinnedLocations.asFlow()

    fun getLocations(): LiveData<List<PinnedLocation>> {
        return locationRepositoryLiveData
    }
    fun setLocations(locations: List<PinnedLocation>) {
        locationRepositoryLiveData.postValue(locations)
    }
    fun addLocation(location: PinnedLocation) {
        val currentLocations = locationRepositoryLiveData.value.orEmpty().toMutableList()
        if (!currentLocations.any { it.id == location.id }) {
            currentLocations.add(location)
        }
        locationRepositoryLiveData.postValue(currentLocations)
    }
    fun removeLocation(location: PinnedLocation) {
        val currentLocations = locationRepositoryLiveData.value.orEmpty().toMutableList()
        currentLocations.remove(location)
        locationRepositoryLiveData.postValue(currentLocations)
    }
}
object UserLocationRepository {
    private val userLocation = MutableLiveData<LatLng>()
    private val cameraPosition = MutableLiveData<CameraPosition>()

    fun getLocation(): LiveData<LatLng> { return userLocation }
    fun setLocation(location: LatLng) { userLocation.postValue(location) }

    fun getCameraPosition(): LiveData<CameraPosition> { return cameraPosition }
    fun setCameraPosition(position: CameraPosition) { cameraPosition.postValue(position) }
}

data class PinnedLocation(
    val id: String,
    val position: LatLng,
    val title: String,
    val snippet: String? = null
)
data class Location(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double
)

val JSON: MediaType = "application/json; charset=utf-8".toMediaType()

class MapViewModel(): ViewModel() {
    //private val _userLocation = MutableStateFlow<LatLng?>(null)
    //val userLocation: StateFlow<LatLng?> = _userLocation.asStateFlow()

    init {
        viewModelScope.launch {
            LocationRepository.pinnedLocationsFlow.collectLatest { currentPinnedLocations ->
                val validLocationIds = currentPinnedLocations.map { it.id }
                StoryRepository.retainStoriesByLocationIds(validLocationIds)
            }
        }
    }

    fun updateUserLocationInActivity(newLocation: LatLng, context: Context) {
        UserLocationRepository.setLocation(newLocation)
        purgeFarLocations(newLocation)
        loadNearestLocation(newLocation, ManifestUtils.getDbUrlFromManifest(context) ?: "http://xlynseyes.ddns.net:3000/")
    }
    var currentCameraPostionState by mutableStateOf<CameraPosition?>(null)
        private set
    val initialCameraPosition = CameraPosition.fromLatLngZoom(LatLng(37.4220, -122.0840), 10f) // Default to GooglePlex

    var currentCameraPositionFromMap by mutableStateOf<CameraPosition?>(null)
        private set

    fun loadNearestLocation(userLocation: LatLng, dbUrl: String) {
        var url = "$dbUrl/nearest_location?latitude=${userLocation.latitude}&longitude=${userLocation.longitude}"
        Log.d("MapViewModel", "loadNearestLocation: $url")
        var request = Request.Builder()
            .url(url)
            .get()
            .build()

        var client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("MapViewModel", "Network request failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBodyString = response.body?.string() // Read body ONCE, this also closes the body

                    if (response.isSuccessful && responseBodyString != null) {
                        val json = JSONObject(responseBodyString)
                        //Log.d("MapViewModel", "Response body: $responseBodyString")
                        //Log.d("MapViewModel", "JSON: $json")
                        val location = json.getJSONObject("nearestLocation")
                        val locationName = location.getString("name")
                        val distance = json.getDouble("distanceKm")
                        val stories = json.getJSONArray("relatedStories")

                        val storyDetails = StringBuilder()

                        val pin = PinnedLocation(
                            id = location.getString("locationId"),
                            position = LatLng(location.getDouble("latitude"), location.getDouble("longitude")),
                            title = location.getString("name")
                        )
                        if (!LocationRepository.getLocations().value.orEmpty().any { it.id == pin.id }) {
                            Log.d("MapViewModel", "Adding pinned location: $pin")
                            LocationRepository.addLocation(pin)
                        }

                        for (i in 0 until stories.length()) {
                            val jsonStory = stories.getJSONObject(i)

                            var story = Story(
                                storyId = jsonStory.getString("storyId"),
                                title = jsonStory.getString("title"),
                                description = jsonStory.getString("description"),
                                dateOfFact = jsonStory.getString("dateOfFact"),
                                photoPath = jsonStory.getString("photoPath"),
                                locationId = jsonStory.getString("locationId"),
                                userId = jsonStory.getString("userId"),
                                author = ""
                            )
                            val existingStories = StoryRepository.getStories().value.orEmpty()
                            if (!existingStories.any { it.storyId == story.storyId }) {
                                Log.d("MapViewModel", "Adding story: $story")
                                StoryRepository.addStory(story)
                            }
                            val title = jsonStory.getString("title")
                            val description = jsonStory.getString("description")

                            storyDetails.append("$title\n$description\n\n")
                        }
                        val message = if (storyDetails.isNotEmpty()) {
                            "Nearest Location: $locationName (${distance}km away)\n\nStories:\n\n$storyDetails"
                        } else {
                            "Nearest Location: $locationName (${distance}km away)\n\nNo stories found."
                        }

                        //_operationStatus.value = "Location get successful!"
                        //Log.d("MapViewModel", "Message: $message")
                        //Log.d("MapViewModel", "Response body: $responseBodyString")
                    }
                } catch (e: Exception) {
                    Log.e("MapViewModel", "Error reading response body", e)
                } finally {
                    //response.body?.string()
                }
            }
    })}
    fun addPinnedLocation(location: PinnedLocation, dbUrl: String) {
        // ?latitude=${location.position.latitude}&longitude=${location.position.longitude}&name=${location.title}&locationId=70d0
        var url = "$dbUrl/add_location"
        var client = OkHttpClient()

        var json = JSONObject()
        json.put("locationId", location.id)
        json.put("name", location.title)
        json.put("latitude", location.position.latitude)
        json.put("longitude", location.position.longitude)

        var request = Request.Builder()
            .url(url)
            .post(json.toString().toRequestBody(JSON))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("MapViewModel", "Network request failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBodyString = response.body?.string()
                if (response.isSuccessful) {
                    Log.e("MapViewModel", "Location added successfully!")
                } else {
                    Log.e("MapViewModel", "Unsuccessful location response: ${response.code} ${response.message}. Body: $responseBodyString")
                }
            }
        })
    }
    fun addStory(story: Story, dbUrl: String) {
        var url = "$dbUrl/add_story"
        var client = OkHttpClient()

        var json = JSONObject()
        json.put("title", story.title)
        json.put("description", story.description)
        json.put("dateOfFact", story.dateOfFact)
        json.put("photoPath", story.photoPath)
        json.put("locationId", story.locationId)
        json.put("userId", story.userId)
        json.put("storyId", story.storyId)


        var request = Request.Builder()
            .url(url)
            .post(json.toString().toRequestBody(JSON))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("MapViewModel", "Network request failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBodyString = response.body?.string()
                if (response.isSuccessful) {
                    Log.d("MapViewModel", "Story added successfully!")
                } else {
                    Log.e("MapViewModel", "Unsuccessful story response: ${response.code} ${response.message}. Body: $responseBodyString")
                }
            }
        })
    }
    fun addReport(storyId: String, dbUrl: String) {
        var url = "$dbUrl/add_report"
        var client = OkHttpClient()

        var json = JSONObject()
        json.put("userId", "70D0")
        json.put("reportId", "report" + storyId + "-" +
                SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()).toString())
        json.put("reason", "This is a report")
        json.put("reportDate", Date().toString())
        json.put("storyId", storyId)

        var request = Request.Builder()
            .url(url)
            .post(json.toString().toRequestBody(JSON))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("MainActivity", "Network request failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("MainActivity", "Report added successfully!")
                } else {
                    Log.e("MainActivity", "Unsuccessful response: ${response.code} ${response.message}")
                }
            }
        })
    }
    fun purgeFarLocations(userLocation: LatLng) {
        val userAndroidLocation = Location("user").apply {
            latitude = userLocation.latitude
            longitude = userLocation.longitude
        }
        val currentPinnedLocations = LocationRepository.getLocations().value.orEmpty()
        val nearbyPinnedLocations = currentPinnedLocations.filter { pinnedLocation ->
            val locationAndroid = Location("pinned").apply {
                latitude = pinnedLocation.position.latitude
                longitude = pinnedLocation.position.longitude
            }
            userAndroidLocation.distanceTo(locationAndroid) / 1000 <= 2 // distance in km
        }
        LocationRepository.setLocations(nearbyPinnedLocations)
        Log.d("MapViewModel", "Purged far locations")
    }

    fun generatePinnedLocation(locationName: String, locationInfo: String? = null): PinnedLocation {
        var locationID = UserLocationRepository.getLocation().value?.latitude.toString() + UserLocationRepository.getLocation().value?.longitude.toString()
        locationID = locationID.replace(".", "")
        Log.d("MapViewModel", "generatePinnedLocation: $locationID")

        return PinnedLocation(
            locationID,
            LatLng(UserLocationRepository.getLocation().value!!.latitude, UserLocationRepository.getLocation().value!!.longitude),
            locationName,
            locationInfo?: null)
    }
    fun generateStory(storyTitle: String, storyDescription: String? = "", storyDate: String, locationId: String) :Story {
        var storyId =
            UserLocationRepository.getLocation().value?.latitude.toString() +
                UserLocationRepository.getLocation().value?.longitude.toString() + "-" +
                SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()).toString()
        storyId = storyId.replace(".", "")
        Log.d("MapViewModel", "generateStory: $storyId")

        return Story(storyId, storyTitle, storyDescription?: "", storyDate, "/images/seattle_underground_ghost.jpg", locationId, "70D0", "70D0")
    }

    fun updateCameraPostionFromMap(position: CameraPosition) {
        currentCameraPositionFromMap = position
    }
    fun saveCurrentCameraPositionToRepository() {
        currentCameraPositionFromMap?.let {
            UserLocationRepository.setCameraPosition(it)
        }
    }

    fun moveToLocation(latLng: LatLng, zoomLevel: Float = 15f) {
        Log.d("MapViewModel", "Moving to location: $latLng")
        currentCameraPostionState = CameraPosition.builder()
            .target(latLng)
            .zoom(zoomLevel)
            .build()
    }
    fun onCameraMoved() {
        currentCameraPostionState = null
    }
}