package com.gizemir.plantapp.domain.repository.forum

import android.net.Uri
import com.gizemir.plantapp.domain.model.forum.Comment
import com.gizemir.plantapp.domain.model.forum.Post
import com.gizemir.plantapp.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface ForumRepository {
    fun getAllPosts(): Flow<List<Post>>
    suspend fun getPostById(postId: String): Post
    suspend fun createPost(content: String, imageUri: Uri?): Post
    suspend fun likePost(postId: String): Post
    suspend fun unlikePost(postId: String): Post
    suspend fun addComment(postId: String, content: String): Comment
    suspend fun getUserPosts(userId: String): List<Post>
    suspend fun deletePost(postId: String): Boolean
    suspend fun updatePost(postId: String, content: String, imageUrl: String? = null): Boolean
    suspend fun deleteComment(commentId: String, postId: String): Boolean
    suspend fun getPosts(): Flow<Resource<List<Post>>>
}
