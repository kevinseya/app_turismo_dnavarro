package com.dnavarro.turismoapp.ui.post

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dnavarro.turismoapp.data.Post
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.CameraPosition

@Composable
fun PostDetailScreen(post: Post) {
    Column(modifier = Modifier.padding(16.dp)) {
        AsyncImage(
            model = post.imageUrls.firstOrNull(),
            contentDescription = post.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = post.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = post.description ?: "", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))
        if (post.latitude != null && post.longitude != null && post.latitude != 0.0 && post.longitude != 0.0) {
            val position = LatLng(post.latitude, post.longitude)
            val cameraPosition = CameraPosition.fromLatLngZoom(position, 15f)
            val cameraPositionState = rememberCameraPositionState { this.position = cameraPosition }
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                cameraPositionState = cameraPositionState
            ) {
                Marker(state = com.google.maps.android.compose.MarkerState(position = position), title = post.title)
            }
        }
    }
}
