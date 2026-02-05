package com.dnavarro.turismoapp.ui.admin

import com.valentinilk.shimmer.shimmer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.shadow
import coil.compose.AsyncImage
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dnavarro.turismoapp.data.Post
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostManagementScreen(navController: NavController, adminViewModel: AdminViewModel, token: String) {
    LaunchedEffect(token) {
        if (token.isNotEmpty()) {
            adminViewModel.getPosts(token)
        }
    }
    val posts by adminViewModel.posts.collectAsState()
    val isLoading = posts.isEmpty()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Posts") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isLoading) {
                items(5) { ShimmerPostItem() }
            } else {
                items(posts) { post ->
                    PostItem(post = post, adminViewModel = adminViewModel, token = token, snackbarHostState = snackbarHostState, navController = navController)
                }
            }
        }
    }
}

@Composable
fun PostItem(post: Post, adminViewModel: AdminViewModel, token: String, snackbarHostState: SnackbarHostState, navController: NavController) {
    var deleteAnim by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scale by animateFloatAsState(
        targetValue = if (deleteAnim) 0.95f else 1f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing), label = "deleteScale"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                // Navegar al detalle del post para ver y borrar comentarios
                val userId = com.dnavarro.turismoapp.data.SessionManager.getUserIdFromToken(token) ?: ""
                navController.navigate("postDetail/${post.id}/$token/$userId")
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (!post.imageUrls.isNullOrEmpty()) {
                AsyncImage(
                    model = post.imageUrls.firstOrNull(),
                    contentDescription = post.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(text = post.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = post.description ?: "", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        scope.launch {
                            deleteAnim = true
                            post.id.let { adminViewModel.deletePost(token, it.toString()) }
                            snackbarHostState.showSnackbar("Post eliminado")
                            deleteAnim = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF66B0E), contentColor = Color.White)
                ) {
                    Text("Eliminar")
                }
            }
        }
    }
}

@Composable
fun ShimmerPostItem() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .shimmer()
            .background(Color.LightGray, RoundedCornerShape(16.dp))
    ) {}
}
