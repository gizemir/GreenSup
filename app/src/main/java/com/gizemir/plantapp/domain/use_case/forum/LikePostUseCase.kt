package com.gizemir.plantapp.domain.use_case.forum

import com.gizemir.plantapp.domain.model.forum.Post
import com.gizemir.plantapp.domain.repository.forum.ForumRepository
import com.gizemir.plantapp.domain.util.Resource
import javax.inject.Inject

class LikePostUseCase @Inject constructor(
    private val repository: ForumRepository
) {
    suspend operator fun invoke(post: Post): Resource<Post> {
        return try {
            val updatedPost = if (post.isLikedByCurrentUser) {
                repository.unlikePost(post.id)
            } else {
                repository.likePost(post.id)
            }
            Resource.Success(updatedPost)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unexpected error occurred")
        }
    }
}
