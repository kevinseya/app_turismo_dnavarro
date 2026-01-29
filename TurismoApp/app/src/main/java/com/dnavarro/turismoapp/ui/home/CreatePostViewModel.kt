package com.dnavarro.turismoapp.ui.home

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnavarro.turismoapp.data.CreatePostRequest
import com.dnavarro.turismoapp.network.Api
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class CreatePostViewModel : ViewModel() {

    private val _selectedImageUris = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImageUris: StateFlow<List<Uri>> = _selectedImageUris

    private val _selectedLocation = MutableStateFlow<LatLng?>(null)
    val selectedLocation: StateFlow<LatLng?> = _selectedLocation

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()

    fun onImageSelected(uri: Uri) {
        _selectedImageUris.value = _selectedImageUris.value + uri
    }

    fun onRemoveImage(uri: Uri) {
        _selectedImageUris.value = _selectedImageUris.value.filter { it != uri }
    }

    fun onLocationSelected(location: LatLng) {
        _selectedLocation.value = location
    }

    fun onTitleChanged(newTitle: String) {
        _title.value = newTitle
    }

    fun onDescriptionChanged(newDescription: String) {
        _description.value = newDescription
    }

    fun onPhoneChanged(newPhone: String) {
        _phone.value = newPhone
    }

    fun createPost(token: String, context: Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val imageUris = _selectedImageUris.value
                val location = _selectedLocation.value
                val title = _title.value
                val description = _description.value
                val phone = _phone.value

                if (imageUris.isEmpty() || location == null) {
                    Log.e("CreatePostViewModel", "Images or location not selected")
                    return@launch
                }

                // 1. Upload images
                val imageParts = imageUris.mapIndexed { idx, uri ->
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val requestFile = inputStream!!.readBytes().toRequestBody("image/jpeg".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("images", "image$idx.jpg", requestFile)
                }

                val imageUrls = Api.retrofitService.uploadImage("Bearer $token", imageParts)

                if (imageUrls.isEmpty()) {
                    Log.e("CreatePostViewModel", "Image upload failed")
                    return@launch
                }

                // 2. Create Post
                val createPostRequest = CreatePostRequest(
                    title = title,
                    description = description,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    phone = phone,
                    images = imageUrls
                )

                Api.retrofitService.createPost("Bearer $token", createPostRequest)

                // 3. Handle success
                onSuccess()

            } catch (t: Throwable) {
                Log.e("CreatePostViewModel", "Failed to create post", t)
            }
        }
    }
}