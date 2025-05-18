package com.localstories.screens

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.UiSettings
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.localstories.viewmodel.MapViewModel

@Composable
fun MapScreen(mapViewModel: MapViewModel) {
    val cameraPositionState = rememberCameraPositionState()
    val context = LocalContext.current
    val userLocation = mapViewModel.userLocation.value
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            mapViewModel.fetchUserLocation(context, fusedLocationClient)
        } else {
            // Handle permission denied
        }
    }

    LaunchedEffect(Unit) {
    when (PackageManager.PERMISSION_GRANTED) {
        ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_FINE_LOCATION ) -> {
            mapViewModel.fetchUserLocation(context, fusedLocationClient)
            }
            else -> {
                permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            compassEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false,
            scrollGesturesEnabled = true
        )
    ) {
        userLocation?.let {
            Marker(
                state = MarkerState(position = it),
                title = "Your Location",
                snippet = "This is your current location"
            )
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
        }
    }
}