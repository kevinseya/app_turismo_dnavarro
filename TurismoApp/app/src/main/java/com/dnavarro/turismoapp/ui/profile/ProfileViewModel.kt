package com.dnavarro.turismoapp.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnavarro.turismoapp.data.Post
import com.dnavarro.turismoapp.data.User
import com.dnavarro.turismoapp.network.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    fun getMyProfile(token: String, userId: String) {
        viewModelScope.launch {
            try {
                // This is a temporary solution. A dedicated endpoint would be better.
                val allPosts = Api.retrofitService.getPosts("Bearer $token")
                val userPosts = allPosts.filter { it.userId.toString() == userId }
                _posts.value = userPosts
                _user.value = userPosts.firstOrNull()?.user

            } catch (t: Throwable) {
                Log.e("ProfileViewModel", "Failed to get profile", t)
            }
        }
    }

    fun deletePost(token: String, postId: String, userId: String) {
        viewModelScope.launch {
            try {
                Api.retrofitService.deletePost("Bearer $token", postId)
                getMyProfile(token, userId) // Refresh the list
            } catch (t: Throwable) {
                Log.e("ProfileViewModel", "Failed to delete post", t)
            }
        }
    }
}
