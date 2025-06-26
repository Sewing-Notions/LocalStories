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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.localstories.viewmodel.LocationRepository
import com.localstories.viewmodel.MapViewModel
import com.localstories.viewmodel.UserLocationRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MapScreen(mapViewModel: MapViewModel) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val pinnedLocationsList by LocationRepository.pinnedLocationsFlow.collectAsState(initial = emptyList())
    var initalUserLocationFetched by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = UserLocationRepository.getCameraPosition().value ?: mapViewModel.initialCameraPosition
    }
    val coroutineScope = rememberCoroutineScope()

    val hasLocationPermission = remember(context) {
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Kat says: CatKISS Gemini
    LaunchedEffect(key1 = hasLocationPermission, key2 = initalUserLocationFetched) {
        if (hasLocationPermission && !initalUserLocationFetched) {
            val lastKnownUserLocation = UserLocationRepository.getLocation().value
            if (lastKnownUserLocation != null) {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(lastKnownUserLocation, 15f),
                    durationMs = 1000
                )
            }
            initalUserLocationFetched = true
        } else  {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    Log.d("MapScreen", "Fetched initial location: ${it.latitude}, ${it.longitude}")
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    mapViewModel.updateUserLocationInActivity(currentLatLng, context)
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f),
                            durationMs = 1000
                        )
                    }
                    initalUserLocationFetched = true
                }
            }.addOnFailureListener { e ->
                Log.e("MapScreen", "Error getting initial location", e)
            }
        }
    }

    LaunchedEffect(key1 = hasLocationPermission) {
        if (hasLocationPermission) {
            while (true) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        Log.d("MapScreen", "Fetched location: ${it.latitude}, ${it.longitude}")
                        val currentLatLng = LatLng(it.latitude, it.longitude)
                        mapViewModel.updateUserLocationInActivity(currentLatLng, context)
                    }
                }.addOnFailureListener { e ->
                    Log.e("MapScreen", "Error getting location", e)
                }
                delay(25000L) // Delay for 25 seconds
            }
        }
    }
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            mapViewModel.updateCameraPostionFromMap(cameraPositionState.position)
        }
    }
    LaunchedEffect(mapViewModel.currentCameraPositionFromMap) {
        mapViewModel.currentCameraPostionState?.let { newPosition ->
            coroutineScope.launch {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newCameraPosition(newPosition),
                    durationMs = 1000 // Animation duration
                )
                mapViewModel.onCameraMoved() // Reset the state in ViewModel
            }
        }
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
    }
}