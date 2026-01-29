package com.dnavarro.turismoapp.ui.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnavarro.turismoapp.data.LoginResponse
import com.dnavarro.turismoapp.data.SessionManager
import com.dnavarro.turismoapp.network.Api
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    fun login(email: String, pass: String, context: Context, onSuccess: (LoginResponse) -> Unit) {
        viewModelScope.launch {
            try {
                val response = Api.retrofitService.login(mapOf("email" to email, "password" to pass))
                response.token?.let { SessionManager.saveToken(context, it) }
                onSuccess(response)
            } catch (t: Throwable) {
                Log.e("LoginViewModel", "Login failed", t)
                // Handle error
            }
        }
    }

    fun register(name: String, email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                Api.retrofitService.register(mapOf("name" to name, "email" to email, "password" to pass))
                onSuccess()
            } catch (t: Throwable) {
                Log.e("LoginViewModel", "Registration failed", t)
                // Handle error
            }
        }
    }
}
