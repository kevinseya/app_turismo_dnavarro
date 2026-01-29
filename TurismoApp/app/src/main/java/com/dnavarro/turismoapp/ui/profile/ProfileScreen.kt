package com.dnavarro.turismoapp.ui.profile

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
    LaunchedEffect(token) {
        if (token.isNotEmpty()) {
            profileViewModel.getMyProfile(token, userId)
        }
    }

    val user by profileViewModel.user.collectAsState()
    val posts by profileViewModel.posts.collectAsState()
    val isLoading = posts.isEmpty()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        user?.let {
            // Animación de foto de perfil
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
                    model = "https://ui-avatars.com/api/?name=" + it.name.replace(" ", "+"),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = it.name,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
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
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Mis Publicaciones",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (isLoading) {
            ShimmerProfilePosts()
        } else {
            MyPostsGrid(posts = posts, onDeleteClick = { postId ->
                profileViewModel.deletePost(token, postId.toString(), userId)
            })
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
fun MyPostsGrid(posts: List<Post>, onDeleteClick: (Int) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(posts) { post ->
            MyPostItem(post = post, onDeleteClick = onDeleteClick)
        }
    }
}

@Composable
fun MyPostItem(post: Post, onDeleteClick: (Int) -> Unit) {
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
                    .clip(RoundedCornerShape(16.dp)),
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

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    TurismoAppTheme {
        val user = User(1, "Turista Explorador", "test@test.com", "CLIENT", true)
        val posts = listOf(
            Post(1, "Un paraíso escondido", "", 0.0, 0.0, "", 0, 0, 0f, 1, listOf("https://images.pexels.com/photos/346529/pexels-photo-346529.jpeg")),
            Post(2, "Aventura en las montañas", "", 0.0, 0.0, "", 0, 0, 0f, 1, listOf("https://images.pexels.com/photos/417074/pexels-photo-417074.jpeg")),
            Post(3, "Ciudad de neón", "", 0.0, 0.0, "", 0, 0, 0f, 1, listOf("https://images.pexels.com/photos/2129796/pexels-photo-2129796.jpeg")),
            Post(4, "Ruta del desierto", "", 0.0, 0.0, "", 0, 0, 0f, 1, listOf("https://images.pexels.com/photos/255469/pexels-photo-255469.jpeg"))
        )
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = user.name,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Mis Publicaciones",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            MyPostsGrid(posts = posts, onDeleteClick = {})
        }
    }
}
