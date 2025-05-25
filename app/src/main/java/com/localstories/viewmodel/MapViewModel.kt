package com.localstories.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PinnedLocation(
    val id: String,
    val position: LatLng,
    val title: String,
    val snippet: String? = null
)

class MapViewModel: ViewModel() {
    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation.asStateFlow()

    fun updateUserLocation(newLocation: LatLng) {
        _userLocation.value = newLocation
    }

    private val _pinnedLocations = MutableStateFlow<List<PinnedLocation>>(emptyList())
    val pinnedLocations: StateFlow<List<PinnedLocation>> = _pinnedLocations.asStateFlow()

    fun loadPinnedLocations() {
        // To do fetch data from database
        _pinnedLocations.value = listOf(
            PinnedLocation(id = "1", position = LatLng(34.0522, -118.2437), title = "Los Angeles", snippet = "City of Angels"),
            PinnedLocation(id = "2", position = LatLng(40.7128, -74.0060), title = "New York", snippet = "The Big Apple"),
            PinnedLocation(id = "3", position = LatLng(37.7749, -122.4194), title = "San Francisco", snippet = "Golden Gate City"),
            PinnedLocation(id = "4", position = LatLng(48.8566, 2.3522), title = "Paris")
        )
    }

    fun addPinnedLocation(location: PinnedLocation) {
        _pinnedLocations.value = _pinnedLocations.value + location
    }
}