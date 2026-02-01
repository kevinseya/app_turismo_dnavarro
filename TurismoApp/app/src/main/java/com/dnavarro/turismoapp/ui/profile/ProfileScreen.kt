
package com.dnavarro.turismoapp.ui.profile
import androidx.compose.foundation.clickable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.valentinilk.shimmer.shimmer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.dnavarro.turismoapp.data.Post
import com.dnavarro.turismoapp.data.User
import com.dnavarro.turismoapp.ui.theme.TurismoAppTheme

@Composable
fun ShimmerProfilePosts() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(4) {
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .shimmer()
                    .background(Color.LightGray, RoundedCornerShape(16.dp))
            ) {}
        }
    }
}

@Composable
fun ProfileScreen(navController: NavController, profileViewModel: ProfileViewModel, token: String, userId: String) {
    // Obtener el id del usuario actual desde el token (puedes ajustar esto según tu lógica de sesión)
    val currentUserId = remember { com.dnavarro.turismoapp.data.SessionManager.getUserIdFromToken(token) ?: "" }
    val isOwnProfile = userId == currentUserId

    LaunchedEffect(token, userId) {
        if (token.isNotEmpty()) {
            profileViewModel.getProfile(token, userId, currentUserId)
        }
    }

    val user by profileViewModel.user.collectAsState()
    val posts by profileViewModel.posts.collectAsState()
    val isFollowing by profileViewModel.isFollowing.collectAsState()
    val isLoading = posts.isEmpty() && user == null
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (user == null && isLoading) {
            CircularProgressIndicator()
        } else if (user != null) {
            val u = user!!
            // Avatar
            var scale = animateFloatAsState(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing), label = "profilePicScale"
            ).value
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = "https://ui-avatars.com/api/?name=" + u.name.replace(" ", "+"),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = u.name,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = u.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "Rol: ${u.role}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))
            // Estadísticas animadas
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedStat("Posts", posts.size)
                AnimatedStat("Likes", posts.sumOf { p -> p.likesCount })
                AnimatedStat("Followers", 0)
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (isOwnProfile) {
                Button(
                    onClick = {
                        com.dnavarro.turismoapp.data.SessionManager.clearSession(context)
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cerrar sesión", color = Color.White)
                }
            } else {
                if (isFollowing == true) {
                    Button(
                        onClick = { profileViewModel.unfollowUser(token, userId, currentUserId) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Dejar de seguir", color = Color.White)
                    }
                } else {
                    Button(
                        onClick = { profileViewModel.followUser(token, userId, currentUserId) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Seguir", color = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = if (isOwnProfile) "Mis Publicaciones" else "Publicaciones",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (isOwnProfile || isFollowing == true) {
                if (posts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay publicaciones", color = MaterialTheme.colorScheme.onBackground)
                    }
                } else {
                    MyPostsGrid(posts = posts, onDeleteClick = { postId ->
                        profileViewModel.deletePost(token, postId.toString(), userId)
                    }, navController = navController)
                }
            } else if (!isOwnProfile && isFollowing == false) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Debes seguir para poder ver el perfil de este usuario", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AnimatedStat(label: String, value: Int) {
    val animatedValue = animateFloatAsState(
        targetValue = value.toFloat(),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing), label = "statAnim"
    ).value
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = animatedValue.toInt().toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun MyPostsGrid(posts: List<Post>, onDeleteClick: (Int) -> Unit, navController: NavController) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(posts) { post ->
            MyPostItem(post = post, onDeleteClick = onDeleteClick, navController = navController)
        }
    }
}

@Composable
fun MyPostItem(post: Post, onDeleteClick: (Int) -> Unit, navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    var scale = animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing), label = "postCardScale"
    ).value
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box {
            AsyncImage(
                model = post.imageUrls.firstOrNull(),
                contentDescription = post.title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { navController.navigate("postDetail/${post.id}") },
                contentScale = ContentScale.Crop
            )
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        scale = 0.9f
                        kotlinx.coroutines.delay(80)
                        scale = 1f
                        onDeleteClick(post.id)
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.5f), shape = MaterialTheme.shapes.small)
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar publicación", tint = Color.White)
            }
        }
    }
}

// Elimina el preview o usa un navController falso si es necesario para compilar
