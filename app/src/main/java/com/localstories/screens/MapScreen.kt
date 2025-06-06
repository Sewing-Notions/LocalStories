package com.localstories.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import androidx.core.app.ActivityCompat
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.localstories.viewmodel.MapViewModel
import kotlinx.coroutines.delay

@Composable
fun MapScreen(mapViewModel: MapViewModel) {
    val cameraPositionState = rememberCameraPositionState()
    val context = LocalContext.current
    val userLocation: LatLng? by mapViewModel.userLocation.collectAsState()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val pinnedLocationsList by mapViewModel.pinnedLocations.collectAsState()

    // Kat says: CatKISS Gemini
    LaunchedEffect(key1 = true) { // key1 = true ensures this runs once on composition
        //mapViewModel.loadNearestLocation(userLocation ?: LatLng(0.0, 0.0), "35.247.54.23", "3000")

        while (true) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        Log.d("MapScreen", "Fetched location: ${it.latitude}, ${it.longitude}")
                        val currentLatLng = LatLng(it.latitude, it.longitude)
                        mapViewModel.updateUserLocationInActivity(currentLatLng) // Update ViewModel
                        // Optionally move the camera to the new location
                        //cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLatLng, 1f)
                    }
                }.addOnFailureListener { e ->
                    Log.e("MapScreen", "Error getting location", e)
                }
            }
            delay(25000L) // Delay for 25 seconds
        }
    }

    val hasLocationPermission = remember(context) {
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = hasLocationPermission
        )
    ) {
        pinnedLocationsList.forEach { pinnedLocation ->
            Marker(
                state = MarkerState(position = pinnedLocation.position),
                title = pinnedLocation.title,
                snippet = pinnedLocation.snippet
            )
        }
        // add other markers
        // mapViewModel.storyMarkers.collectAsState().value.forEach { storyMarker ->
        //     Marker(
        //         state = MarkerState(position = storyMarker.latLng),
        //         title = storyMarker.title
        //     )
        //
    }
}