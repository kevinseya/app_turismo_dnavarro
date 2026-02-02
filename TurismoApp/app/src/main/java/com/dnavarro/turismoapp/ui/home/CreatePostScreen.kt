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
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.LocationOn
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.activity.result.ActivityResultLauncher
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

    // Permisos
    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionDialogMessage by remember { mutableStateOf("") }

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempCameraUri != null) {
                createPostViewModel.onImageSelected(tempCameraUri!!)
            }
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            uris?.forEach { createPostViewModel.onImageSelected(it) }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val uri = ComposeFileProvider.getImageUri(context)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            permissionDialogMessage = "Se requiere permiso de cámara para tomar fotos."
            showPermissionDialog = true
        }
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            navController.navigate("mapScreen")
        } else {
            permissionDialogMessage = "Se requiere permiso de ubicación para seleccionar la ubicación."
            showPermissionDialog = true
        }
    }

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
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permiso requerido") },
            text = { Text(permissionDialogMessage) },
            confirmButton = {
                Button(onClick = { showPermissionDialog = false }) {
                    Text("Aceptar")
                }
            }
        )
    }



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



            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            val uri = ComposeFileProvider.getImageUri(context)
                            tempCameraUri = uri
                            cameraLauncher.launch(uri)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = "Tomar foto", modifier = Modifier.size(48.dp))
                }
                IconButton(
                    onClick = {
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Seleccionar imágenes", modifier = Modifier.size(48.dp))
                }
                IconButton(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            navController.navigate("mapScreen")
                        } else {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(Icons.Default.AddLocation, contentDescription = if (selectedLocation == null) "Añadir ubicación" else "Ubicación seleccionada", modifier = Modifier.size(48.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                            // Navegar al feed tras éxito
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Publicar")
            }
        }
    }
}
