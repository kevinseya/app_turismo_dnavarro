package com.dnavarro.turismoapp.ui.admin
import com.valentinilk.shimmer.shimmer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.dnavarro.turismoapp.data.User
import com.dnavarro.turismoapp.ui.theme.TurismoAppTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun UserManagementScreen(navController: NavController, adminViewModel: AdminViewModel, token: String) {
    LaunchedEffect(token) {
        if (token.isNotEmpty()) {
            adminViewModel.getUsers(token)
        }
    }
    val users by adminViewModel.users.collectAsState()
    val isLoading = users.isEmpty()
    val snackbarHostState = remember { SnackbarHostState() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "Gestión de Usuarios",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (isLoading) {
            ShimmerUserList()
        } else {
            UserList(users = users, adminViewModel = adminViewModel, token = token, snackbarHostState = snackbarHostState)
        }
        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
fun UserList(users: List<User>, modifier: Modifier = Modifier, adminViewModel: AdminViewModel, token: String, snackbarHostState: SnackbarHostState) {
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(users) { user ->
            UserItem(user = user, adminViewModel = adminViewModel, token = token, snackbarHostState = snackbarHostState)
        }
    }
}

@Composable
fun UserItem(user: User, adminViewModel: AdminViewModel, token: String, snackbarHostState: SnackbarHostState) {
    var blockAnim by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scale by animateFloatAsState(
        targetValue = if (blockAnim) 0.95f else 1f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing), label = "blockScale"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .graphicsLayer(scaleX = scale, scaleY = scale),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = user.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text(text = user.email, style = MaterialTheme.typography.bodyMedium)
            Text(text = "Rol: ${user.role}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        scope.launch {
                            blockAnim = true
                            if (user.isActive == true) {
                                adminViewModel.blockUser(token, user.id)
                                snackbarHostState.showSnackbar("Usuario bloqueado")
                            } else {
                                adminViewModel.unblockUser(token, user.id)
                                snackbarHostState.showSnackbar("Usuario desbloqueado")
                            }
                            blockAnim = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isActive == true) Color(0xFFF66B0E) else Color(0xFF205375),
                        contentColor = Color.White
                    )
                ) {
                    Text(if (user.isActive == true) "Bloquear" else "Desbloquear")
                }
                if (user.role == "CLIENT") {
                    Button(onClick = {
                        scope.launch {
                            adminViewModel.changeUserRole(token, user.id, "ADMIN")
                            snackbarHostState.showSnackbar("Ahora es Admin")
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF205375), contentColor = Color.White)) {
                        Text("Hacer Admin")
                    }
                } else {
                    Button(onClick = {
                        scope.launch {
                            adminViewModel.changeUserRole(token, user.id, "CLIENT")
                            snackbarHostState.showSnackbar("Rol cambiado a Cliente")
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF66B0E), contentColor = Color.White)) {
                        Text("Quitar Admin")
                    }
                }
            }
        }
    }
}

@Composable
fun ShimmerUserList() {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(5) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .shimmer()
                    .background(Color.LightGray, RoundedCornerShape(16.dp))
            ) {}
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserManagementScreenPreview() {
    val sampleUsers = listOf(
        User(1, "Kevin Chancusi", "kevin@test.com", "CLIENT", true),
        User(2, "Admin Turismo", "admin@turismo.com", "ADMIN", true),
        User(3, "Turista Explorador", "turista@test.com", "CLIENT", false)
    )
    val snackbarHostState = remember { SnackbarHostState() }
    TurismoAppTheme {
        UserList(users = sampleUsers, adminViewModel = viewModel(), token = "", snackbarHostState = snackbarHostState)
    }
}
