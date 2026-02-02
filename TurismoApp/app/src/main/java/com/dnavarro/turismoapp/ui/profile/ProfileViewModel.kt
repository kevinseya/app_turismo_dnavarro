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

    private val _isFollowing = MutableStateFlow<Boolean?>(null)
    val isFollowing: StateFlow<Boolean?> = _isFollowing

    fun getProfile(token: String, userId: String, currentUserId: String) {
        viewModelScope.launch {
            try {
                val userProfile = Api.retrofitService.getUserProfile("Bearer $token", userId)
                _user.value = userProfile
                val userPosts = Api.retrofitService.getPostsByUser("Bearer $token", userId)
                _posts.value = userPosts
                // Si el perfil no es el propio, revisa si lo sigues
                _isFollowing.value = if (userId == currentUserId) null else userProfile.isFollowing == true
            } catch (t: Throwable) {
                Log.e("ProfileViewModel", "Failed to get profile", t)
            }
        }
    }

    fun followUser(token: String, userId: String, currentUserId: String) {
        viewModelScope.launch {
            try {
                Api.retrofitService.followUser("Bearer $token", userId)
                getProfile(token, userId, currentUserId)
            } catch (t: Throwable) {
                Log.e("ProfileViewModel", "Failed to follow user", t)
            }
        }
    }

    fun unfollowUser(token: String, userId: String, currentUserId: String) {
        viewModelScope.launch {
            try {
                Api.retrofitService.unfollowUser("Bearer $token", userId)
                getProfile(token, userId, currentUserId)
            } catch (t: Throwable) {
                Log.e("ProfileViewModel", "Failed to unfollow user", t)
            }
        }
    }

    fun deletePost(token: String, postId: String, userId: String) {
        viewModelScope.launch {
            try {
                Api.retrofitService.deletePost("Bearer $token", postId)
                getProfile(token, userId, userId) // Refresh the list
            } catch (t: Throwable) {
                Log.e("ProfileViewModel", "Failed to delete post", t)
            }
        }
    }
}
