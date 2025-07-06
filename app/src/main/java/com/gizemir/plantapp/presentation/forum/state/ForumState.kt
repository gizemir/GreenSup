package com.gizemir.plantapp.presentation.forum.state

import com.gizemir.plantapp.domain.model.forum.Post

data class ForumState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
