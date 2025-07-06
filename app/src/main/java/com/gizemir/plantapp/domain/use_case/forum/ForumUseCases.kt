package com.gizemir.plantapp.domain.use_case.forum

import com.gizemir.plantapp.domain.model.forum.Post
import com.gizemir.plantapp.domain.util.Resource
import javax.inject.Inject

data class ForumUseCases @Inject constructor(
    val getPosts: GetPostsUseCase,
    val getPostById: GetPostByIdUseCase,
    val createPost: CreatePostUseCase,
    val likePost: LikePostUseCase,
    val addComment: AddCommentUseCase,
    val deleteComment: DeleteCommentUseCase
)








