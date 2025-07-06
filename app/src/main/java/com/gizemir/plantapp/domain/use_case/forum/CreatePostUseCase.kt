package com.gizemir.plantapp.domain.use_case.forum

import android.net.Uri
import com.gizemir.plantapp.domain.model.forum.Post
import com.gizemir.plantapp.domain.repository.forum.ForumRepository
import com.gizemir.plantapp.domain.util.Resource
import javax.inject.Inject

class CreatePostUseCase @Inject constructor(
    private val repository: ForumRepository
) {
    suspend operator fun invoke(content: String, imageUri: Uri?): Resource<Post> {
        return try {
            if (content.isBlank() && imageUri == null) {
                return Resource.Error("Post must have either text or an image")
            }
            
            val post = repository.createPost(content, imageUri)
            Resource.Success(post)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unexpected error occurred")
        }
    }
}
