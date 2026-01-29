package com.dnavarro.turismoapp.ui.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnavarro.turismoapp.data.AdminDashboardResponse
import com.dnavarro.turismoapp.data.Post
import com.dnavarro.turismoapp.data.User
import com.dnavarro.turismoapp.network.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _dashboardResponse = MutableStateFlow<AdminDashboardResponse?>(null)
    val dashboardResponse: StateFlow<AdminDashboardResponse?> = _dashboardResponse

    fun getAdminDashboard(token: String) {
        viewModelScope.launch {
            try {
                val response = Api.retrofitService.getAdminDashboard("Bearer $token")
                _dashboardResponse.value = response
                _posts.value = response.topPosts
            } catch (t: Throwable) {
                Log.e("AdminViewModel", "Failed to get admin dashboard", t)
            }
        }
    }

    fun getUsers(token: String) {
        viewModelScope.launch {
            try {
                val response = Api.retrofitService.getUsers("Bearer $token")
                _users.value = response
            } catch (t: Throwable) {
                Log.e("AdminViewModel", "Failed to get users", t)
            }
        }
    }

    fun getPosts(token: String) {
        viewModelScope.launch {
            try {
                val response = Api.retrofitService.getPosts("Bearer $token")
                _posts.value = response
            } catch (t: Throwable) {
                Log.e("AdminViewModel", "Failed to get posts", t)
            }
        }
    }

    fun deletePost(token: String, postId: String) {
        viewModelScope.launch {
            try {
                Api.retrofitService.deletePost("Bearer $token", postId)
                getPosts(token) // Refresh the list
            } catch (t: Throwable) {
                Log.e("AdminViewModel", "Failed to delete post", t)
            }
        }
    }

    fun blockUser(token: String, userId: Int) {
        viewModelScope.launch {
            try {
                Api.retrofitService.blockUser("Bearer $token", userId.toString())
                getUsers(token) // Refresh the list
            } catch (t: Throwable) {
                Log.e("AdminViewModel", "Failed to block user", t)
            }
        }
    }

    fun unblockUser(token: String, userId: Int) {
        viewModelScope.launch {
            try {
                Api.retrofitService.unblockUser("Bearer $token", userId.toString())
                getUsers(token) // Refresh the list
            } catch (t: Throwable) {
                Log.e("AdminViewModel", "Failed to unblock user", t)
            }
        }
    }

    fun changeUserRole(token: String, userId: Int, role: String) {
        viewModelScope.launch {
            try {
                Api.retrofitService.changeUserRole("Bearer $token", userId.toString(), mapOf("role" to role))
                getUsers(token) // Refresh the list
            } catch (t: Throwable) {
                Log.e("AdminViewModel", "Failed to change user role", t)
            }
        }
    }
}
