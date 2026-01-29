package com.dnavarro.turismoapp.network

import com.dnavarro.turismoapp.data.* 
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import retrofit2.Retrofit
import retrofit2.http.*

private const val BASE_URL = "http://10.0.2.2:3000/" // Replace with your actual backend URL
const val BASE_IMAGE_URL = "http://10.0.2.2:3000/"

private val json = Json { ignoreUnknownKeys = true }

private val retrofit = Retrofit.Builder()
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(BASE_URL)
    .build()

interface ApiService {
    // Auth
    @POST("auth/register")
    suspend fun register(@Body user: Map<String, String>): RegisterResponse

    @POST("auth/login")
    suspend fun login(@Body credentials: Map<String, String>): LoginResponse

    // Users
    @GET("users")
    suspend fun getUsers(@Header("Authorization") token: String): List<User>

    @PATCH("users/{id}/block")
    suspend fun blockUser(@Header("Authorization") token: String, @Path("id") userId: String)

    @PATCH("users/{id}/unblock")
    suspend fun unblockUser(@Header("Authorization") token: String, @Path("id") userId: String)

    @PATCH("users/{id}/role")
    suspend fun changeUserRole(@Header("Authorization") token: String, @Path("id") userId: String, @Body role: Map<String, String>)

    @GET("users/{id}")
    suspend fun getUserProfile(@Header("Authorization") token: String, @Path("id") userId: String): User

    @POST("users/follow/{id}")
    suspend fun followUser(@Header("Authorization") token: String, @Path("id") userId: String)

    @DELETE("users/follow/{id}")
    suspend fun unfollowUser(@Header("Authorization") token: String, @Path("id") userId: String)

    // Posts
    @Multipart
    @POST("posts/upload")
    suspend fun uploadImage(
        @Header("Authorization") token: String,
        @Part images: List<MultipartBody.Part>
    ): List<String>

    @POST("posts")
    suspend fun createPost(
        @Header("Authorization") token: String,
        @Body post: CreatePostRequest
    ): Post

    @GET("posts")
    suspend fun getPosts(@Header("Authorization") token: String): List<Post>

    @DELETE("posts/{id}")
    suspend fun deletePost(@Header("Authorization") token: String, @Path("id") postId: String)

    // Likes
    @POST("likes/{postId}")
    suspend fun likePost(@Header("Authorization") token: String, @Path("postId") postId: String)

    @DELETE("likes/{postId}")
    suspend fun unlikePost(@Header("Authorization") token: String, @Path("postId") postId: String)

    // Comments
    @POST("posts/{postId}/comments")
    suspend fun createComment(
        @Header("Authorization") token: String, 
        @Path("postId") postId: String, 
        @Body request: CreateCommentRequest
    ): Comment

    @GET("posts/{postId}/comments")
    suspend fun getComments(@Header("Authorization") token: String, @Path("postId") postId: String): List<Comment>

    // Feed
    @GET("feed")
    suspend fun getFeed(@Header("Authorization") token: String): List<Post>

    @GET("feed/nearby")
    suspend fun getNearbyFeed(@Header("Authorization") token: String, @Query("lat") lat: Double, @Query("lng") lng: Double, @Query("radiusKm") radiusKm: Int): List<Post>

    // Notifications
    @POST("notifications/send")
    suspend fun sendNotification(@Header("Authorization") token: String, @Body notification: Map<String, String>)

    @GET("notifications/my")
    suspend fun getMyNotifications(@Header("Authorization") token: String): List<Notification>

    // Dashboard
    @GET("admin/dashboard")
    suspend fun getAdminDashboard(@Header("Authorization") token: String): AdminDashboardResponse
}

object Api {
    val retrofitService: ApiService by lazy { retrofit.create(ApiService::class.java) }
}
