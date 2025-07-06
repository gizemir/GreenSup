package com.gizemir.plantapp.data.source.forum

import android.net.Uri
import com.gizemir.plantapp.data.remote.dto.forum.CommentDto
import com.gizemir.plantapp.data.remote.dto.forum.PostDto
import kotlinx.coroutines.flow.Flow

interface ForumRemoteDataSource {
    fun getAllPosts(): Flow<List<PostDto>>
    suspend fun getPostById(postId: String): PostDto
    suspend fun createPost(userId: String, content: String, imageUri: Uri?): PostDto
    suspend fun likePost(postId: String, userId: String): Int
    suspend fun unlikePost(postId: String, userId: String): Int
    suspend fun isPostLikedByUser(postId: String, userId: String): Boolean
    suspend fun addComment(postId: String, userId: String, content: String): CommentDto
    suspend fun getCommentsForPost(postId: String): List<CommentDto>
    suspend fun getUserPosts(userId: String): List<PostDto>
    suspend fun deletePost(postId: String): Boolean
    suspend fun updatePost(postId: String, content: String, imageUrl: String?): Boolean
    suspend fun deleteComment(commentId: String, postId: String): Boolean
    suspend fun uploadImage(imageUri: Uri, path: String): String
}
