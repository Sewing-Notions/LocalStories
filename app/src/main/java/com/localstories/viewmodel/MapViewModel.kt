package com.localstories.viewmodel

import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.kotlin.localDate
import com.localstories.Story
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation.asStateFlow()
    private val _nearbyStories = MutableStateFlow<List<Story>>(emptyList())
    val nearbyStories: StateFlow<List<Story>> = _nearbyStories.asStateFlow()

    fun updateUserLocationInActivity(newLocation: LatLng) {
        _userLocation.value = newLocation
        purgeFarLocations(newLocation)
        loadNearestLocation(newLocation, "35.247.54.23", "3000")
    }
    var cameraPostionState by mutableStateOf<CameraPosition?>(null)
        private set
    val initialCameraPosition = CameraPosition.fromLatLngZoom(LatLng(37.4220, -122.0840), 10f) // Default to GooglePlex

    private val _pinnedLocations = MutableStateFlow<List<PinnedLocation>>(emptyList())
    val pinnedLocations: StateFlow<List<PinnedLocation>> = _pinnedLocations.asStateFlow()

    fun loadNearestLocation(userLocation: LatLng, ip: String, port: String) {
        var url = "http://$ip:$port/nearest_location?latitude=${userLocation.latitude}&longitude=${userLocation.longitude}"
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
                        Log.d("MapViewModel", "Response body: $responseBodyString")
                        Log.d("MapViewModel", "JSON: $json")
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
                        if (!_pinnedLocations.value.any { it.id == pin.id }) {
                            Log.d("MapViewModel", "Adding pinned location: $pin")
                            _pinnedLocations.value = _pinnedLocations.value + pin
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
                            if (!_nearbyStories.value.any { it.storyId == story.storyId }) {
                                Log.d("MapViewModel", "Adding story: $story")
                                _nearbyStories.value = _nearbyStories.value + story
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
                        Log.d("MapViewModel", "Message: $message")
                        Log.d("MapViewModel", "Response body: $responseBodyString")
                    }
                } catch (e: Exception) {
                    Log.e("MapViewModel", "Error reading response body", e)
                } finally {
                    //response.body?.string()
                }
            }
    })}
    fun addPinnedLocation(location: PinnedLocation, ip: String, port: String) {
        // ?latitude=${location.position.latitude}&longitude=${location.position.longitude}&name=${location.title}&locationId=70d0
        var url = "http://$ip:$port/add_location"
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
    fun addStory(story: Story, ip: String, port: String) {
        var url = "http://$ip:$port/add_story"
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
    fun addReport(storyId: String) {
        var url = "http://35.247.54.23:3000/add_report"
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
        val userAndroidLocation = Location("").apply {
            latitude = userLocation.latitude
            longitude = userLocation.longitude
        }
        _pinnedLocations.value = _pinnedLocations.value.filter { pinnedLocation ->
            val locationAndroid = Location("").apply {
                latitude = pinnedLocation.position.latitude
                longitude = pinnedLocation.position.longitude
            }
            userAndroidLocation.distanceTo(locationAndroid) / 1000 <= 2 // distance in km
        }
        Log.d("MapViewModel", "Purged far locations")
    }

    fun generatePinnedLocation(locationName: String, locationInfo: String? = null): PinnedLocation {
        var locationID = userLocation.value?.latitude.toString() + userLocation.value?.longitude.toString()
        locationID = locationID.replace(".", "")
        Log.d("MapViewModel", "generatePinnedLocation: $locationID")

        return PinnedLocation(locationID, LatLng(userLocation.value!!.latitude, userLocation.value!!.longitude), locationName, locationInfo?: null)
    }
    fun generateStory(storyTitle: String, storyDescription: String? = "", storyDate: String, locationId: String) :Story {
        var storyId =
            userLocation.value?.latitude.toString() +
                userLocation.value?.longitude.toString() + "-" +
                SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()).toString()
        storyId = storyId.replace(".", "")
        Log.d("MapViewModel", "generateStory: $storyId")

        return Story(storyId, storyTitle, storyDescription?: "", storyDate, "/images/seattle_underground_ghost.jpg", locationId, "70D0", "70D0")
    }

    fun moveToLocation(latLng: LatLng, zoomLevel: Float = 15f) {
        cameraPostionState = CameraPosition.builder()
            .target(latLng)
            .zoom(zoomLevel)
            .build()
    }
    fun onCameraMoved() {
        cameraPostionState = null
    }
}