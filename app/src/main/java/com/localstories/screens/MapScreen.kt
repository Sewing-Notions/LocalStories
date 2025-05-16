package com.localstories.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import androidx.compose.runtime.remember
import com.google.maps.android.compose.GoogleMap
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

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
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