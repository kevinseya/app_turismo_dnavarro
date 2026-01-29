package com.dnavarro.turismoapp.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.dnavarro.turismoapp.data.Post
import com.valentinilk.shimmer.shimmer

@Composable
fun ShimmerPostItem() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .height(320.dp)
            .shimmer()
            .background(Color.LightGray, RoundedCornerShape(16.dp))
    ) {}
}

@Composable
fun HomeScreen(homeViewModel: HomeViewModel, navController: NavController, token: String) {
    LaunchedEffect(token) {
        if (token.isNotEmpty()) {
            homeViewModel.getFeed(token)
        }
    }
    val posts by homeViewModel.posts.collectAsState()
    val isLoading = posts.isEmpty()
    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            if (isLoading) {
                items(4) { ShimmerPostItem() }
            } else {
                items(posts) { post ->
                    PostItem(
                        post = post,
                        homeViewModel = homeViewModel,
                        token = token,
                        navController = navController,
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostItem(
    post: Post,
    homeViewModel: HomeViewModel,
    token: String,
    navController: NavController,
    snackbarHostState: SnackbarHostState
) {
    var likeAnim by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (likeAnim) 1.3f else 1f,
        animationSpec = spring(dampingRatio = 0.4f), label = "likeScale"
    )
    var showLikeSnackbar by remember { mutableStateOf(false) }
    var showFollowSnackbar by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column {
            // Galería swipeable de imágenes
            val images = post.imageUrls
            val pagerState = rememberPagerState(pageCount = { images.size.coerceAtLeast(1) })
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)) {
                HorizontalPager(state = pagerState) { page ->
                    AsyncImage(
                        model = images.getOrNull(page),
                        contentDescription = "Imagen ${page + 1} de la publicación",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                // Indicadores de página
                if (images.size > 1) {
                    Row(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(images.size) { i ->
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = post.user?.name ?: "Usuario desconocido",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                // Botón Seguir animado (simple)
                var isFollowing by remember { mutableStateOf(false) }
                val followColor by animateColorAsState(
                    targetValue = if (isFollowing) Color.Gray else MaterialTheme.colorScheme.primary,
                    animationSpec = tween(durationMillis = 400), label = "followColor"
                )
                Button(
                    onClick = {
                        isFollowing = !isFollowing
                        if (!isFollowing) {
                            homeViewModel.followUser(token, post.userId!!)
                            showFollowSnackbar = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = followColor)
                ) {
                    Text(if (isFollowing) "Siguiendo" else "Seguir")
                }
            }
            Text(
                text = post.title,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(
                text = post.description ?: "",
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    if (post.isLiked) {
                        homeViewModel.unlikePost(token, post.id)
                    } else {
                        homeViewModel.likePost(token, post.id)
                        likeAnim = true
                        showLikeSnackbar = true
                    }
                }, modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)) {
                    Icon(
                        imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.isLiked) Color.Red else Color.Gray
                    )
                }
                // Reset animación después de un rebote
                if (likeAnim) LaunchedEffect(likeAnim) {
                    kotlinx.coroutines.delay(300)
                    likeAnim = false
                }
                // Snackbars para feedback visual
                if (showLikeSnackbar) LaunchedEffect(showLikeSnackbar) {
                    snackbarHostState.showSnackbar("¡Te gustó esta publicación!")
                    showLikeSnackbar = false
                }
                if (showFollowSnackbar) LaunchedEffect(showFollowSnackbar) {
                    snackbarHostState.showSnackbar("¡Ahora sigues a este usuario!")
                    showFollowSnackbar = false
                }
                Text(text = "${post.likesCount} Me gusta")
                Spacer(modifier = Modifier.width(16.dp))
                IconButton(onClick = { navController.navigate("comments/${post.id}") }) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Comentarios"
                    )
                }
                Text(text = "${post.commentsCount} Comentarios")
            }
        }
    }
}
