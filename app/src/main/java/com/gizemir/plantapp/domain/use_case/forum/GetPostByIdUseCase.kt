package com.gizemir.plantapp.domain.use_case.forum

import com.gizemir.plantapp.domain.model.forum.Post
import com.gizemir.plantapp.domain.repository.forum.ForumRepository
import javax.inject.Inject

class GetPostByIdUseCase @Inject constructor(
    private val repository: ForumRepository
) {
    suspend operator fun invoke(postId: String): Post {
        return repository.getPostById(postId)
    }
} 