package com.dnavarro.turismoapp.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.dnavarro.turismoapp.ComposeFileProvider
import com.dnavarro.turismoapp.ui.theme.TurismoAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(navController: NavController, createPostViewModel: CreatePostViewModel, token: String) {
    val title by createPostViewModel.title.collectAsState()
    val description by createPostViewModel.description.collectAsState()
    val phone by createPostViewModel.phone.collectAsState()
    val selectedLocation by createPostViewModel.selectedLocation.collectAsState()
    val selectedImageUris by createPostViewModel.selectedImageUris.collectAsState()
    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Error de validación") },
            text = { Text(dialogMessage) },
            confirmButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Aceptar")
                }
            }
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            // Si la cámara devuelve una imagen, agregarla
            if (success) {
                // ComposeFileProvider.getImageUri(context) se usó para lanzar
                // pero no se almacena, así que no se puede recuperar aquí
                // Mejor usar un callback para obtener el uri antes
            }
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            uris?.forEach { createPostViewModel.onImageSelected(it) }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear nueva publicación") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { createPostViewModel.onTitleChanged(it) },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { createPostViewModel.onDescriptionChanged(it) },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { createPostViewModel.onPhoneChanged(it) },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (selectedImageUris.isNotEmpty()) {
                val pagerState = rememberPagerState(pageCount = { selectedImageUris.size })
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    HorizontalPager(state = pagerState) { page ->
                        Box {
                            AsyncImage(
                                model = selectedImageUris[page],
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp)),
                                contentDescription = "Imagen seleccionada ${page + 1}",
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { createPostViewModel.onRemoveImage(selectedImageUris[page]) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(50))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar imagen", tint = Color.White)
                            }
                        }
                    }
                    // Indicadores de página
                    if (selectedImageUris.size > 1) {
                        Row(
                            Modifier
                                .align(Alignment.BottomCenter)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(selectedImageUris.size) { i ->
                                val selected = pagerState.currentPage == i
                                Box(
                                    Modifier
                                        .padding(horizontal = 2.dp)
                                        .size(if (selected) 10.dp else 7.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(if (selected) MaterialTheme.colorScheme.primary else Color.LightGray)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Button(
                    onClick = {
                        // Selección múltiple de galería
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Seleccionar imágenes")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Elegir imágenes")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Button(onClick = { navController.navigate("mapScreen") }) {
                    Icon(Icons.Default.AddLocation, contentDescription = "Añadir ubicación")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedLocation == null) "Añadir ubicación" else "Ubicación seleccionada")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = {
                    // Aquí deberías pedir permiso y obtener la ubicación actual
                    // Por simplicidad, simula una ubicación (ejemplo: LatLng de CDMX)
                    createPostViewModel.onLocationSelected(com.google.android.gms.maps.model.LatLng(19.4326, -99.1332))
                }) {
                    Text("Usar mi ubicación")
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    if (title.isBlank()) {
                        dialogMessage = "Por favor, ingresa un título."
                        showDialog = true
                    } else if (description.isBlank()) {
                        dialogMessage = "Por favor, ingresa una descripción."
                        showDialog = true
                    } else if (phone.isBlank()) {
                        dialogMessage = "Por favor, ingresa un teléfono."
                        showDialog = true
                    } else if (selectedImageUris.isEmpty()) {
                        dialogMessage = "Por favor, selecciona al menos una imagen."
                        showDialog = true
                    } else if (selectedLocation == null) {
                        dialogMessage = "Por favor, selecciona una ubicación."
                        showDialog = true
                    } else {
                        createPostViewModel.createPost(token, context) {
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "Publicar",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun CreatePostScreenPreview() {
    TurismoAppTheme {
        CreatePostScreen(rememberNavController(), viewModel(), "")
    }
}
