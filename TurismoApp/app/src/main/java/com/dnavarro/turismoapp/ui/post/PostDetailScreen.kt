package com.dnavarro.turismoapp.ui.post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.dnavarro.turismoapp.data.Post
import com.dnavarro.turismoapp.data.Comment
import com.dnavarro.turismoapp.ui.home.HomeViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.MarkerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    navController: NavController,
    postId: String,
    token: String,
    userId: String
) {
    val viewModel: PostDetailViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()
    
    LaunchedEffect(postId, token) {
        viewModel.loadPost(token, postId)
    }

    val post by viewModel.post.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var newComment by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Post") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (post != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Cabecera con foto y nombre del autor
                    item {
                        post!!.user?.let { author ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navController.navigate("profile/$token/${author.id}/$userId")
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val profileImageUrl = if (author.profileImage != null) {
                                    com.dnavarro.turismoapp.network.BASE_IMAGE_URL + author.profileImage
                                } else {
                                    "https://ui-avatars.com/api/?name=${author.name.replace(" ", "+")}"
                                }
                                AsyncImage(
                                    model = profileImageUrl,
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .padding(end = 8.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = author.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    // Galería de imágenes
                    item {
                        if (post!!.images.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(post!!.imageUrls) { imageUrl ->
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = post!!.title,
                                        modifier = Modifier
                                            .width(300.dp)
                                            .height(250.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }

                    // Título y descripción
                    item {
                        Column {
                            Text(
                                text = post!!.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = post!!.description ?: "",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    // Acciones (likes y comentarios)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            IconButton(onClick = {
                                if (post!!.isLiked) {
                                    homeViewModel.unlikePost(token, post!!.id)
                                    viewModel.updateLike(false)
                                } else {
                                    homeViewModel.likePost(token, post!!.id)
                                    viewModel.updateLike(true)
                                }
                            }) {
                                Icon(
                                    imageVector = if (post!!.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Me gusta",
                                    tint = if (post!!.isLiked) Color.Red else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "${post!!.likesCount} Me gusta",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    // Mapa
                    item {
                        if (post!!.latitude != null && post!!.longitude != null &&
                            post!!.latitude != 0.0 && post!!.longitude != 0.0
                        ) {
                            val position = LatLng(post!!.latitude!!, post!!.longitude!!)
                            val cameraPosition = CameraPosition.fromLatLngZoom(position, 15f)
                            val cameraPositionState = rememberCameraPositionState {
                                this.position = cameraPosition
                            }
                            GoogleMap(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                cameraPositionState = cameraPositionState
                            ) {
                                Marker(
                                    state = MarkerState(position = position),
                                    title = post!!.title
                                )
                            }
                        }
                    }

                    // Sección de comentarios
                    item {
                        Text(
                            text = "Comentarios",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    if (comments.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No hay comentarios aún",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(comments) { comment ->
                            CommentDetailItem(
                                comment = comment,
                                token = token,
                                postId = postId,
                                userId = userId,
                                viewModel = viewModel
                            )
                        }
                    }
                }

                // Campo de nuevo comentario
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newComment,
                        onValueChange = { newComment = it },
                        label = { Text("Escribe un comentario...") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = {
                        scope.launch {
                            viewModel.addComment(token, postId, newComment)
                            newComment = ""
                            snackbarHostState.showSnackbar("¡Comentario enviado!")
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Enviar comentario"
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Post no encontrado")
            }
        }
    }
}

@Composable
fun CommentDetailItem(
    comment: Comment,
    token: String,
    postId: String,
    userId: String,
    viewModel: PostDetailViewModel
) {
    val profileViewModel: com.dnavarro.turismoapp.ui.profile.ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val user by profileViewModel.user.collectAsState()

    LaunchedEffect(Unit) {
        if (user == null) {
            profileViewModel.getProfile(token, userId, userId)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = comment.user?.name ?: "Usuario desconocido",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = comment.content, style = MaterialTheme.typography.bodyMedium)
                }
                
                // Mostrar botón de borrar si es admin o propietario del comentario
                if (user?.role == "ADMIN" || comment.userId.toString() == userId) {
                    IconButton(onClick = {
                        viewModel.deleteComment(token, postId, comment.id)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Borrar comentario",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
