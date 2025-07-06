package com.gizemir.plantapp.domain.repository.forum

import android.net.Uri
import com.gizemir.plantapp.domain.model.forum.Post

interface PostRepository {
    suspend fun getAllPosts(): List<Post>
    suspend fun getPostById(postId: String): Post
    suspend fun createPost(content: String, imageUri: Uri?): Post
    suspend fun likePost(postId: String): Post
    suspend fun addComment(postId: String, commentText: String): Post
    suspend fun getUserPosts(userId: String): List<Post>
}

