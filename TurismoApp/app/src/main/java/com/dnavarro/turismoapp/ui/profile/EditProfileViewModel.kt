package com.dnavarro.turismoapp.ui.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnavarro.turismoapp.data.User
import com.dnavarro.turismoapp.network.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class EditProfileViewModel : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadUserProfile(token: String, userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _user.value = Api.retrofitService.getUserProfile("Bearer $token", userId)
            } catch (t: Throwable) {
                Log.e("EditProfileViewModel", "Failed to load user profile", t)
                _errorMessage.value = "Error al cargar el perfil"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(token: String, name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                val updatedUser = Api.retrofitService.updateProfile(
                    "Bearer $token",
                    mapOf("name" to name)
                )
                _user.value = updatedUser
                onSuccess()
            } catch (t: Throwable) {
                Log.e("EditProfileViewModel", "Failed to update profile", t)
                _errorMessage.value = "Error al actualizar el perfil"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadProfileImage(token: String, uri: Uri, context: Context, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                // Copiar el archivo de la URI a un archivo temporal
                val inputStream = context.contentResolver.openInputStream(uri)
                val tempFile = File(context.cacheDir, "profile_image_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(tempFile)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                val requestBody = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("image", tempFile.name, requestBody)

                val updatedUser = Api.retrofitService.uploadProfileImage("Bearer $token", part)
                _user.value = updatedUser

                // Limpiar el archivo temporal
                tempFile.delete()
                
                onSuccess()
            } catch (t: Throwable) {
                Log.e("EditProfileViewModel", "Failed to upload profile image", t)
                _errorMessage.value = "Error al subir la imagen"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
