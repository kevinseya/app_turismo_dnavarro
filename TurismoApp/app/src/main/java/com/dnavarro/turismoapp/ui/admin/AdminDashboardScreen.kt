package com.dnavarro.turismoapp.ui.admin

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.dnavarro.turismoapp.data.AdminDashboardResponse
import com.dnavarro.turismoapp.ui.theme.TurismoAppTheme
import com.valentinilk.shimmer.shimmer

@Composable
fun AdminDashboardScreen(
    navController: NavController,
    adminViewModel: AdminViewModel,
    token: String
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    LaunchedEffect(token) {
        if (token.isNotEmpty()) {
            adminViewModel.getAdminDashboard(token)
        }
    }

    val dashboardResponse by adminViewModel.dashboardResponse.collectAsState()
    val isLoading = dashboardResponse == null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Panel de Administrador",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            ShimmerDashboardStats()
        } else {
            dashboardResponse?.let {
                StatisticsGrid(it)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DashboardCard(
                modifier = Modifier.weight(1f),
                title = "Gestionar Usuarios",
                icon = Icons.Default.Group,
                onClick = { navController.navigate("userManagement/$token") }
            )
            DashboardCard(
                modifier = Modifier.weight(1f),
                title = "Gestionar Posts",
                icon = Icons.Default.Article,
                onClick = { navController.navigate("postManagement/$token") }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                com.dnavarro.turismoapp.data.SessionManager.clearSession(context)
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth(0.5f)
        ) {
            Text("Cerrar Sesión", color = Color.White)
        }
    }
}

@Composable
fun StatisticsGrid(response: AdminDashboardResponse) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCardAnimated(
                modifier = Modifier.weight(1f),
                title = "Usuarios",
                value = response.totalUsers,
                icon = Icons.Default.Group
            )
            StatCardAnimated(
                modifier = Modifier.weight(1f),
                title = "Posts",
                value = response.totalPosts,
                icon = Icons.Default.Article
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCardAnimated(
                modifier = Modifier.weight(1f),
                title = "Comentarios",
                value = response.totalComments,
                icon = Icons.Default.Comment
            )
            StatCardAnimated(
                modifier = Modifier.weight(1f),
                title = "Likes",
                value = response.totalLikes,
                icon = Icons.Default.ThumbUp
            )
        }
    }
}

@Composable
fun StatCardAnimated(
    modifier: Modifier = Modifier,
    title: String,
    value: Int,
    icon: ImageVector
) {
    val animatedValue by animateFloatAsState(
        targetValue = value.toFloat(),
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "statAnim"
    )

    Card(
        modifier = modifier.shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = title)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = animatedValue.toInt().toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(text = title)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(150.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = title, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ShimmerDashboardStats() {
    Column {
        repeat(2) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp)
                            .shimmer()
                            .background(Color.LightGray, RoundedCornerShape(16.dp))
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminDashboardScreenPreview() {
    TurismoAppTheme {
        AdminDashboardScreen(
            rememberNavController(),
            viewModel(),
            ""
        )
    }
}
