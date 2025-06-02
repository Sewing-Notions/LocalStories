package com.localstories.viewmodel

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
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

data class PinnedLocation(
    val id: String,
    val position: LatLng,
    val title: String,
    val snippet: String? = null
)

val JSON: MediaType = "application/json; charset=utf-8".toMediaType()

class MapViewModel: ViewModel() {
    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation.asStateFlow()

    // Add a StateFlow to communicate messages/errors to the UI
    private val _operationStatus = MutableStateFlow<String?>(null)
    val operationStatus: StateFlow<String?> = _operationStatus.asStateFlow()

    fun clearOperationStatus() {
        _operationStatus.value = null
    }

    fun updateUserLocation(newLocation: LatLng) {
        _userLocation.value = newLocation

        loadNearestLocation(newLocation, "35.247.54.23", "3000")
        purgeFarLocations(newLocation)
    }
    fun getUserLocation(): LatLng {
        return _userLocation.value!!
    }

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
                _operationStatus.value = "Failed to get story: ${e.message}"
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBodyString = response.body?.string() // Read body ONCE, this also closes the body

                    if (response.isSuccessful && responseBodyString != null) {
                        val json = JSONObject(responseBodyString)
                        val location = json.getJSONObject("nearestLocation")
                        val locationName = location.getString("name")
                        val distance = json.getDouble("distanceKm")
                        val stories = json.getJSONArray("relatedStories")

                        val storyDetails = StringBuilder()
                        for (i in 0 until stories.length()) {
                            val story = stories.getJSONObject(i)
                            val title = story.getString("title")
                            val description = story.getString("description")

                            val pin = PinnedLocation(
                                id = location.getString("locationId"),
                                position = LatLng(location.getDouble("latitude"), location.getDouble("longitude")),
                                title = location.getString("name")
                            )
                            if (!_pinnedLocations.value.any { it.id == pin.id }) {
                                Log.d("MapViewModel", "Adding pinned location: $pin")
                                _pinnedLocations.value = _pinnedLocations.value + pin
                            }

                            storyDetails.append("$title\n$description\n\n")
                        }
                        val message = if (storyDetails.isNotEmpty()) {
                            "Nearest Location: $locationName (${distance}km away)\n\nStories:\n\n$storyDetails"
                        } else {
                            "Nearest Location: $locationName (${distance}km away)\n\nNo stories found."
                        }

                        //_operationStatus.value = "Location get successful!"
                        //Log.d("MapViewModel", "Response body: $responseBodyString")
                    }
                } catch (e: Exception) {
                    Log.e("MapViewModel", "Error reading response body", e)
                    _operationStatus.value = "Error processing response."
                } finally {
                    //response.body?.string()
                }
            }
            /*
        _pinnedLocations.value = listOf(
            PinnedLocation(id = "1", position = LatLng(34.0522, -118.2437), title = "Los Angeles", snippet = "City of Angels"),
            PinnedLocation(id = "2", position = LatLng(40.7128, -74.0060), title = "New York", snippet = "The Big Apple"),
            PinnedLocation(id = "3", position = LatLng(37.7749, -122.4194), title = "San Francisco", snippet = "Golden Gate City"),
            PinnedLocation(id = "4", position = LatLng(48.8566, 2.3522), title = "Paris")
        ) */
    })}

    fun addPinnedLocation(location: PinnedLocation, ip: String, port: String) {
        // ?latitude=${location.position.latitude}&longitude=${location.position.longitude}&name=${location.title}&locationId=70d0
        var url = "http://$ip:$port/add_location"
        var client = OkHttpClient()

        var body = "".toRequestBody(JSON)

        var request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("MainActivity", "Network request failed", e)
                _operationStatus.value = "Failed to add location: ${e.message}"
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    _operationStatus.value = "Location added successfully!"
                } else {
                    Log.e("MainActivity", "Unsuccessful response: ${response.code} ${response.message}")
                    _operationStatus.value = "Server error: ${response.message} (Code: ${response.code})"
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
        return PinnedLocation("70D0", getUserLocation(), locationName, locationInfo?: null)
    }
}