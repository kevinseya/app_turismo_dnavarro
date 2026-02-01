package com.dnavarro.turismoapp.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnavarro.turismoapp.data.Post
import com.dnavarro.turismoapp.network.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

        fun searchUsers(token: String, query: String, onSuccess: (List<com.dnavarro.turismoapp.data.User>) -> Unit, onError: (String) -> Unit) {
            viewModelScope.launch {
                try {
                    // Por ahora, filtra localmente tras obtener todos los usuarios
                    val allUsers = com.dnavarro.turismoapp.network.Api.retrofitService.getUsers("Bearer $token")
                    val filtered = if (query.isBlank()) allUsers else allUsers.filter {
                        it.name.contains(query, ignoreCase = true) || (it.email?.contains(query, ignoreCase = true) ?: false)
                    }
                    onSuccess(filtered)
                } catch (t: Throwable) {
                    onError("Error al buscar usuarios: ${t.localizedMessage}")
                }
            }
        }
    fun incrementCommentsCount(postId: Int?) {
        if (postId == null) return
        _posts.value = _posts.value.map {
            if (it.id == postId) it.copy(commentsCount = it.commentsCount + 1) else it
        }
    }

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    fun getFeed(token: String) {
        viewModelScope.launch {
            try {
                _posts.value = Api.retrofitService.getPosts("Bearer $token")
            } catch (t: Throwable) {
                Log.e("HomeViewModel", "Failed to get feed", t)
            }
        }
    }

    fun likePost(token: String, postId: Int) {
        viewModelScope.launch {
            try {
                Api.retrofitService.likePost("Bearer $token", postId.toString())
                _posts.value = _posts.value.map {
                    if (it.id == postId) {
                        it.copy(isLiked = true, likesCount = it.likesCount + 1)
                    } else {
                        it
                    }
                }
            } catch (t: Throwable) {
                Log.e("HomeViewModel", "Failed to like post", t)
            }
        }
    }

    fun unlikePost(token: String, postId: Int) {
        viewModelScope.launch {
            try {
                Api.retrofitService.unlikePost("Bearer $token", postId.toString())
                _posts.value = _posts.value.map {
                    if (it.id == postId) {
                        it.copy(isLiked = false, likesCount = it.likesCount - 1)
                    } else {
                        it
                    }
                }
            } catch (t: Throwable) {
                Log.e("HomeViewModel", "Failed to unlike post", t)
            }
        }
    }

    fun followUser(token: String, userId: Int) {
        viewModelScope.launch {
            try {
                Api.retrofitService.followUser("Bearer $token", userId.toString())
            } catch (t: Throwable) {
                Log.e("HomeViewModel", "Failed to follow user", t)
            }
        }
    }

    fun unfollowUser(token: String, userId: Int) {
        viewModelScope.launch {
            try {
                Api.retrofitService.unfollowUser("Bearer $token", userId.toString())
            } catch (t: Throwable) {
                Log.e("HomeViewModel", "Failed to unfollow user", t)
            }
        }
    }
}
