package com.dnavarro.turismoapp.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dnavarro.turismoapp.ui.theme.DarkBlue
import com.dnavarro.turismoapp.ui.theme.LightGray
import com.dnavarro.turismoapp.ui.theme.Orange

@Composable
fun LoginScreen(navController: NavController, loginViewModel: LoginViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Mostrar Snackbar si hay mensaje
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBlue)
    ) {
        // SnackbarHost
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bienvenido a TurismoApp",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Inicia sesión para continuar",
                style = MaterialTheme.typography.bodyLarge.copy(color = LightGray)
            )
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = LightGray,
                    unfocusedContainerColor = LightGray,
                    disabledContainerColor = LightGray,
                    focusedIndicatorColor = Orange,
                    focusedTextColor = DarkBlue,
                    unfocusedTextColor = DarkBlue,
                    cursorColor = DarkBlue
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = LightGray,
                    unfocusedContainerColor = LightGray,
                    disabledContainerColor = LightGray,
                    focusedIndicatorColor = Orange,
                    focusedTextColor = DarkBlue,
                    unfocusedTextColor = DarkBlue,
                    cursorColor = DarkBlue
                )
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    // Validaciones
                    val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
                    when {
                        email.isBlank() || password.isBlank() -> {
                            snackbarMessage = "Por favor, completa todos los campos."
                        }
                        !emailPattern.matches(email) -> {
                            snackbarMessage = "Ingresa un email válido."
                        }
                        else -> {
                            isLoading = true
                            loginViewModel.login(email, password, context, onSuccess = { loginResponse ->
                                isLoading = false
                                val user = loginResponse.user
                                val token = loginResponse.token
                                if (user != null && token != null) {
                                    snackbarMessage = "¡Inicio de sesión exitoso!"
                                    if (user.role == "ADMIN") {
                                        navController.navigate("adminDashboard/$token") {
                                            popUpTo("login") { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    } else {
                                        navController.navigate("postList/$token/${user.id}") {
                                            popUpTo("login") { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                } else {
                                    snackbarMessage = loginResponse.message ?: "Credenciales incorrectas."
                                }
                            }, onError = { errorMsg ->
                                isLoading = false
                                snackbarMessage = errorMsg ?: "Error al iniciar sesión."
                            })
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Orange)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Iniciar Sesión",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            ClickableText(
                text = AnnotatedString("¿No tienes una cuenta? Regístrate"),
                onClick = { navController.navigate("register") },
                style = TextStyle(color = LightGray, fontSize = 14.sp)
            )
        }
    }
}
