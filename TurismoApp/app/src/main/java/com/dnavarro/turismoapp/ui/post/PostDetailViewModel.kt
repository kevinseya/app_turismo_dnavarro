package com.dnavarro.turismoapp.ui.post

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnavarro.turismoapp.data.Comment
import com.dnavarro.turismoapp.data.CreateCommentRequest
import com.dnavarro.turismoapp.data.Post
import com.dnavarro.turismoapp.network.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PostDetailViewModel : ViewModel() {
    private val _post = MutableStateFlow<Post?>(null)
    val post: StateFlow<Post?> = _post

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadPost(token: String, postId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val postData = Api.retrofitService.getPostById("Bearer $token", postId)
                _post.value = postData
                loadComments(token, postId)
            } catch (t: Throwable) {
                Log.e("PostDetailViewModel", "Failed to load post", t)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadComments(token: String, postId: String) {
        viewModelScope.launch {
            try {
                _comments.value = Api.retrofitService.getComments("Bearer $token", postId)
            } catch (t: Throwable) {
                Log.e("PostDetailViewModel", "Failed to load comments", t)
            }
        }
    }

    fun addComment(token: String, postId: String, content: String) {
        viewModelScope.launch {
            try {
                val request = CreateCommentRequest(content = content, rating = 5.0f)
                Api.retrofitService.createComment("Bearer $token", postId, request)
                loadComments(token, postId)
                // Actualizar contador de comentarios
                _post.value = _post.value?.copy(commentsCount = _post.value!!.commentsCount + 1)
            } catch (t: Throwable) {
                Log.e("PostDetailViewModel", "Failed to add comment", t)
            }
        }
    }

    fun deleteComment(token: String, postId: String, commentId: Int) {
        viewModelScope.launch {
            try {
                Api.retrofitService.deleteComment("Bearer $token", postId, commentId.toString())
                loadComments(token, postId)
                // Actualizar contador de comentarios
                _post.value = _post.value?.copy(commentsCount = maxOf(0, _post.value!!.commentsCount - 1))
            } catch (t: Throwable) {
                Log.e("PostDetailViewModel", "Failed to delete comment", t)
            }
        }
    }

    fun updateLike(isLiked: Boolean) {
        _post.value = _post.value?.copy(
            isLiked = isLiked,
            likesCount = if (isLiked) _post.value!!.likesCount + 1 else _post.value!!.likesCount - 1
        )
    }
}
