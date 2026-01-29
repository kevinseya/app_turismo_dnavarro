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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@Composable
fun MapScreen(navController: NavController, createPostViewModel: CreatePostViewModel) {
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
            // Botón para centrar en ubicación actual (simulado, puedes conectar GPS real)
            FloatingActionButton(
                onClick = {
                    // Simula ubicación actual (CDMX), reemplaza por GPS real si lo deseas
                    val current = LatLng(19.4326, -99.1332)
                    markerState.position = current
                    cameraPositionState.move(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(current, 16f)))
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
