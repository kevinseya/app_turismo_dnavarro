package com.dnavarro.turismoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dnavarro.turismoapp.ui.admin.AdminDashboardScreen
import com.dnavarro.turismoapp.ui.admin.AdminViewModel
import com.dnavarro.turismoapp.ui.admin.PostManagementScreen
import com.dnavarro.turismoapp.ui.admin.UserManagementScreen
import com.dnavarro.turismoapp.ui.home.* 
import com.dnavarro.turismoapp.ui.login.LoginScreen
import com.dnavarro.turismoapp.ui.login.LoginViewModel
import com.dnavarro.turismoapp.ui.login.RegisterScreen
import com.dnavarro.turismoapp.ui.profile.NotificationsScreen
import com.dnavarro.turismoapp.ui.profile.NotificationsViewModel
import com.dnavarro.turismoapp.ui.profile.ProfileScreen
import com.dnavarro.turismoapp.ui.profile.ProfileViewModel
import com.dnavarro.turismoapp.ui.theme.TurismoAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TurismoAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
                    val loginViewModel: LoginViewModel = viewModel()
                    val homeViewModel: HomeViewModel = viewModel()
                    val adminViewModel: AdminViewModel = viewModel()
                    val createPostViewModel: CreatePostViewModel = viewModel()
                    val profileViewModel: ProfileViewModel = viewModel()
                    val commentsViewModel: CommentsViewModel = viewModel()
                    val notificationsViewModel: NotificationsViewModel = viewModel()
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") {
                            LoginScreen(navController = navController, loginViewModel = loginViewModel)
                        }
                        composable("register") {
                            RegisterScreen(navController = navController, loginViewModel = loginViewModel)
                        }
                        composable(
                            "postList/{token}/{userId}",
                            arguments = listOf(
                                navArgument("token") { type = NavType.StringType },
                                navArgument("userId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val token = backStackEntry.arguments?.getString("token") ?: ""
                            val userId = backStackEntry.arguments?.getString("userId") ?: ""
                            PostScreen(homeViewModel = homeViewModel, token = token, userId = userId, navController = navController)
                        }
                        composable(
                            "adminDashboard/{token}",
                            arguments = listOf(navArgument("token") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val token = backStackEntry.arguments?.getString("token") ?: ""
                            AdminDashboardScreen(navController = navController, adminViewModel = adminViewModel, token = token)
                        }
                        composable(
                            "userManagement/{token}",
                            arguments = listOf(navArgument("token") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val token = backStackEntry.arguments?.getString("token") ?: ""
                            UserManagementScreen(navController = navController, adminViewModel = adminViewModel, token = token)
                        }
                        composable(
                            "postManagement/{token}",
                            arguments = listOf(navArgument("token") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val token = backStackEntry.arguments?.getString("token") ?: ""
                            PostManagementScreen(navController = navController, adminViewModel = adminViewModel, token = token)
                        }
                        composable(
                            "createPost/{token}",
                            arguments = listOf(navArgument("token") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val token = backStackEntry.arguments?.getString("token") ?: ""
                            CreatePostScreen(navController = navController, createPostViewModel = createPostViewModel, token = token)
                        }
                        composable("mapScreen") {
                            MapScreen(navController = navController, createPostViewModel = createPostViewModel)
                        }
                        composable(
                            "profile/{token}/{userId}",
                            arguments = listOf(
                                navArgument("token") { type = NavType.StringType },
                                navArgument("userId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val token = backStackEntry.arguments?.getString("token") ?: ""
                            val userId = backStackEntry.arguments?.getString("userId") ?: ""
                            ProfileScreen(navController = navController, profileViewModel = profileViewModel, token = token, userId = userId)
                        }
                        composable(
                            "comments/{token}/{postId}",
                            arguments = listOf(
                                navArgument("token") { type = NavType.StringType },
                                navArgument("postId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val token = backStackEntry.arguments?.getString("token") ?: ""
                            val postId = backStackEntry.arguments?.getString("postId") ?: ""
                            CommentsScreen(navController = navController, commentsViewModel = commentsViewModel, token = token, postId = postId)
                        }
                        composable(
                            "notifications/{token}",
                            arguments = listOf(navArgument("token") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val token = backStackEntry.arguments?.getString("token") ?: ""
                            NotificationsScreen(navController = navController, notificationsViewModel = notificationsViewModel, token = token)
                        }
                    }
                }
            }
        }
    }
}
