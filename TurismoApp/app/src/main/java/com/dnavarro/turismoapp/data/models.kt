package com.dnavarro.turismoapp.data

import com.dnavarro.turismoapp.network.BASE_IMAGE_URL
import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: String = "",
    val isActive: Boolean? = null,
    val isFollowing: Boolean? = null,
    val profileImage: String? = null,
    val followersCount: Int? = null,
    val followingCount: Int? = null
)

@Serializable
data class Post(
    val id: Int,
    val title: String,
    val description: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val phone: String? = null,
    val likesCount: Int,
    val commentsCount: Int,
    val ratingAvg: Float? = null,
    val userId: Int? = null,
    val images: List<String> = emptyList(),
    val user: User? = null,
    val isLiked: Boolean = false
) {
    val imageUrls: List<String>
        get() = images.map { BASE_IMAGE_URL + it }
}

@Serializable
data class Comment(
    val id: Int,
    val content: String,
    val rating: Float,
    val userId: Int,
    val postId: Int,
    val user: User? = null
)

@Serializable
data class Like(
    val id: Int,
    val userId: Int,
    val postId: Int
)

@Serializable
data class Follow(
    val id: Int,
    val followerId: Int,
    val followingId: Int
)

@Serializable
data class Notification(
    val id: Int,
    val title: String,
    val message: String,
    val userId: Int,
    val type: String? = null,
    val postId: Int? = null,
    val commentId: Int? = null,
    val createdAt: String
)

@Serializable
data class LoginResponse(
    @SerialName("access_token")
    val token: String? = null,
    val message: String? = null,
    val user: User? = null
)

@Serializable
data class RegisterResponse(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val isActive: Boolean? = null,
    val createdAt: String? = null
)

@Serializable
data class CreatePostRequest(
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val phone: String,
    val images: List<String>
)

@Serializable
data class CreateCommentRequest(
    val content: String,
    val rating: Float
)

@Serializable
data class AdminDashboardResponse(
    val totalUsers: Int,
    val totalPosts: Int,
    val totalComments: Int,
    val totalLikes: Int,
    val topPosts: List<Post>
)
