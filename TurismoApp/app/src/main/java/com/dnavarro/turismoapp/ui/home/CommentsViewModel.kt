package com.dnavarro.turismoapp.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnavarro.turismoapp.data.Comment
import com.dnavarro.turismoapp.data.CreateCommentRequest
import com.dnavarro.turismoapp.network.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CommentsViewModel : ViewModel() {

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    fun getComments(token: String, postId: String) {
        viewModelScope.launch {
            try {
                _comments.value = Api.retrofitService.getComments("Bearer $token", postId)
            } catch (t: Throwable) {
                Log.e("CommentsViewModel", "Failed to get comments", t)
            }
        }
    }

    fun createComment(token: String, postId: String, content: String, onCommentAdded: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val request = CreateCommentRequest(content = content, rating = 5.0f) // Default rating
                Api.retrofitService.createComment("Bearer $token", postId, request)
                getComments(token, postId) // Refresh the list
                onCommentAdded?.invoke()
            } catch (t: Throwable) {
                Log.e("CommentsViewModel", "Failed to create comment", t)
            }
        }
    }
}
