package com.gizemir.plantapp.domain.use_case.forum

import com.gizemir.plantapp.domain.model.forum.Comment
import com.gizemir.plantapp.domain.repository.forum.ForumRepository
import javax.inject.Inject

class DeleteCommentUseCase @Inject constructor(
    private val repository: ForumRepository
) {
    suspend operator fun invoke(comment: Comment): Boolean {
        return repository.deleteComment(comment.id, comment.postId)
    }
} 