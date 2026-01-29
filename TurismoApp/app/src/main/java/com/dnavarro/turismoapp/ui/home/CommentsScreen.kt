package com.dnavarro.turismoapp.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import com.valentinilk.shimmer.shimmer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.dnavarro.turismoapp.data.Comment
import com.dnavarro.turismoapp.data.User
import com.dnavarro.turismoapp.ui.theme.TurismoAppTheme
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.background
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(navController: NavController, commentsViewModel: CommentsViewModel, token: String, postId: String) {
    var newComment by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(postId) {
        if (token.isNotEmpty()) {
            commentsViewModel.getComments(token, postId)
        }
    }
    val comments by commentsViewModel.comments.collectAsState()
    val isLoading = comments.isEmpty()
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Comentarios") },
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
        ) {
            LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isLoading) {
                    items(4) { ShimmerCommentItem() }
                } else {
                    items(comments) { comment ->
                        CommentItem(comment = comment)
                    }
                }
            }
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
                        commentsViewModel.createComment(token, postId, newComment)
                        newComment = ""
                        snackbarHostState.showSnackbar("¡Comentario enviado!")
                    }
                }) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Enviar comentario")
                }
            }
        }
    }
}

@Composable
fun ShimmerCommentItem() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shimmer()
            .background(Color.LightGray, RoundedCornerShape(12.dp))
    ) {}
}

@Composable
fun CommentItem(comment: Comment) {
    var likeAnim by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (likeAnim) 1.2f else 1f,
        animationSpec = spring(dampingRatio = 0.5f), label = "likeScaleComment"
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = comment.user?.name ?: "Usuario desconocido",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = comment.content, style = MaterialTheme.typography.bodyMedium)
            // Si el backend soporta likes en comentarios, aquí iría el botón
            /*
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    // commentsViewModel.likeComment(...)
                    likeAnim = true
                }, modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = "Like", tint = Color.Gray)
                }
                if (likeAnim) LaunchedEffect(likeAnim) {
                    kotlinx.coroutines.delay(250)
                    likeAnim = false
                }
            }
            */
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommentsScreenPreview() {
    val sampleUser1 = User(1, "Aventurera Viajera", "test1@test.com", "CLIENT", true)
    val sampleUser2 = User(2, "Gourmet Errante", "test2@test.com", "CLIENT", true)
    val sampleComments = listOf(
        Comment(1, "¡Qué fotaza! ¿Dónde es exactamente?", 5f, 1, 1, sampleUser1),
        Comment(2, "¡Guau! Necesito añadir ese lugar a mi lista de deseos. Gracias por compartir.", 5f, 2, 1, sampleUser2)
    )
    TurismoAppTheme {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(sampleComments) { comment ->
                CommentItem(comment = comment)
            }
        }
    }
}
