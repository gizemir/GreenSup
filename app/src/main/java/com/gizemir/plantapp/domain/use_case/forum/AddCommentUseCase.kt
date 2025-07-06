package com.gizemir.plantapp.domain.use_case.forum

import com.gizemir.plantapp.domain.model.forum.Comment
import com.gizemir.plantapp.domain.repository.forum.ForumRepository
import com.gizemir.plantapp.domain.util.Resource
import javax.inject.Inject

class AddCommentUseCase @Inject constructor(
    private val repository: ForumRepository
) {
    suspend operator fun invoke(postId: String, content: String): Resource<Comment> {
        return try {
            if (content.isBlank()) {
                return Resource.Error("Comment cannot be empty")
            }
            
            val comment = repository.addComment(postId, content)
            Resource.Success(comment)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unexpected error occurred")
        }
    }
}
