package com.dnavarro.turismoapp.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.dnavarro.turismoapp.data.Post
import com.dnavarro.turismoapp.data.User
import com.dnavarro.turismoapp.ui.theme.TurismoAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(modifier: Modifier = Modifier, homeViewModel: HomeViewModel, token: String, userId: String, navController: NavController) {
    LaunchedEffect(token) {
        if (token.isNotEmpty()) {
            homeViewModel.getFeed(token)
        }
    }
    val posts by homeViewModel.posts.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("TurismoApp") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { navController.navigate("notifications/$token") }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notificaciones",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = { navController.navigate("searchUsers/$token") }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar personas",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = { navController.navigate("profile/$token/$userId") }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("createPost/$token") }) {
                Icon(Icons.Default.Add, contentDescription = "Crear nueva publicación")
            }
        }
    ) { paddingValues ->
        PostList(
            posts = posts,
            modifier = Modifier.padding(paddingValues),
            onLikeClick = {
                if (it.isLiked) {
                    homeViewModel.unlikePost(token, it.id)
                } else {
                    homeViewModel.likePost(token, it.id)
                }
            },
            onCommentClick = { post ->
                val currentUserId = com.dnavarro.turismoapp.data.SessionManager.getUserIdFromToken(token) ?: ""
                navController.navigate("postDetail/${post.id}/$token/$currentUserId")
            },
            onUserClick = { post ->
                val authorId = post.user?.id
                if (authorId != null) {
                    navController.navigate("profile/$token/$authorId")
                }
            }
        )
    }
}

@Composable
fun PostList(
    posts: List<Post>,
    modifier: Modifier = Modifier,
    onLikeClick: (Post) -> Unit,
    onCommentClick: (Post) -> Unit,
    onUserClick: (Post) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(posts) { post ->
            PostItem(post = post, onLikeClick = onLikeClick, onCommentClick = onCommentClick, onUserClick = onUserClick)
        }
    }
}

@Composable
fun PostItem(
    post: Post,
    modifier: Modifier = Modifier,
    onLikeClick: (Post) -> Unit,
    onCommentClick: (Post) -> Unit,
    onUserClick: (Post) -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            post.user?.let {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clickable { onUserClick(post) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val profileImageUrl = if (it.profileImage != null) {
                        com.dnavarro.turismoapp.network.BASE_IMAGE_URL + it.profileImage
                    } else {
                        "https://ui-avatars.com/api/?name=${it.name.replace(" ", "+")}"
                    }
                    
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val imageRequest = coil.request.ImageRequest.Builder(context)
                        .data(profileImageUrl)
                        .memoryCachePolicy(coil.request.CachePolicy.DISABLED)
                        .diskCachePolicy(coil.request.CachePolicy.DISABLED)
                        .build()
                    
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = it.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            AsyncImage(
                model = post.imageUrls.firstOrNull(),
                contentDescription = "Imagen de la publicación: ${post.title}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clickable { onCommentClick(post) },
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = post.description ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(onClick = { onLikeClick(post) }) {
                    Icon(
                        imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Me gusta",
                        tint = if (post.isLiked) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = "${post.likesCount}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Box(
                    contentAlignment = Alignment.TopEnd
                ) {
                    IconButton(onClick = { onCommentClick(post) }) {
                        Icon(
                            imageVector = Icons.Default.Comment,
                            contentDescription = "Comentarios",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    if (post.commentsCount > 0) {
                        Text(
                            text = "${post.commentsCount}",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-4).dp, y = 4.dp)
                                .background(Color.White, shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PostScreenPreview() {
    val sampleUser1 = User(1, "Turista Explorador", "test1@test.com", "CLIENT", true)
    val sampleUser2 = User(2, "Aventurera Viajera", "test2@test.com", "CLIENT", true)
    val samplePosts = listOf(
        Post(
            id = 1,
            title = "Un paraíso escondido",
            description = "Descubrí esta playa secreta en mi último viaje. ¡El agua es cristalina y la arena es blanca!",
            latitude = 0.0,
            longitude = 0.0,
            phone = "",
            likesCount = 152,
            commentsCount = 12,
            ratingAvg = 4.8f,
            userId = 1,
            images = listOf("346529/pexels-photo-346529.jpeg"),
            user = sampleUser1
        ),
        Post(
            id = 2,
            title = "Aventura en las montañas",
            description = "Las vistas desde la cima de esta montaña son simplemente espectaculares. ¡Una caminata que vale la pena!",
            latitude = 0.0,
            longitude = 0.0,
            phone = "",
            likesCount = 218,
            commentsCount = 25,
            ratingAvg = 4.9f,
            userId = 2,
            images = listOf("417074/pexels-photo-417074.jpeg"),
            user = sampleUser2
        )
    )
    TurismoAppTheme {
        PostList(posts = samplePosts, onLikeClick = {}, onCommentClick = {}, onUserClick = {})
    }
}
