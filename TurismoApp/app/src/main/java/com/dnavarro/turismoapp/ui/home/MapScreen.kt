package com.dnavarro.turismoapp.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dnavarro.turismoapp.ui.theme.TurismoAppTheme
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@SuppressLint("MissingPermission")
@Composable
fun MapScreen(navController: NavController, createPostViewModel: CreatePostViewModel) {
    val context = LocalContext.current
    var fusedLocationClient by remember { mutableStateOf<FusedLocationProviderClient?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }
    // Estado inicial: si ya hay ubicación seleccionada, usarla, si no, Bogotá
    val initialLatLng = createPostViewModel.selectedLocation.collectAsState().value ?: LatLng(4.60971, -74.08175)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLatLng, 13f)
    }
    val markerState = rememberMarkerState(position = initialLatLng)


    Scaffold { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng -> markerState.position = latLng }
            ) {
                Marker(
                    state = markerState,
                    draggable = true
                )
            }
            // Botón para centrar en ubicación actual (GPS real)
            FloatingActionButton(
                onClick = {
                    fusedLocationClient?.lastLocation?.addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            val current = LatLng(location.latitude, location.longitude)
                            markerState.position = current
                            cameraPositionState.move(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(current, 16f)))
                        } else {
                            locationError = "No se pudo obtener la ubicación actual."
                        }
                    }?.addOnFailureListener {
                        locationError = "Error al obtener la ubicación: ${it.localizedMessage}"
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Icon(imageVector = Icons.Default.MyLocation, contentDescription = "Mi ubicación")
            }
            // Botón para confirmar ubicación
            FloatingActionButton(
                onClick = {
                    createPostViewModel.onLocationSelected(markerState.position)
                    navController.popBackStack()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Confirmar ubicación")
            }
            // Mostrar error si ocurre
            if (locationError != null) {
                androidx.compose.material3.Snackbar(
                    modifier = Modifier.align(Alignment.TopCenter),
                    action = {},
                    content = { androidx.compose.material3.Text(locationError!!) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MapScreenPreview() {
    TurismoAppTheme {
        // MapScreen(rememberNavController(), viewModel())
    }
}
