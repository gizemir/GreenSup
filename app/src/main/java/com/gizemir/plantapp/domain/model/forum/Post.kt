package com.gizemir.plantapp.domain.model.forum

import java.util.Date
import java.util.UUID

data class Post(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val userName: String,
    val userProfilePicUrl: String? = null,
    val content: String,
    val imageUrl: String? = null,
    val timestamp: Date = Date(),
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val isLikedByCurrentUser: Boolean = false,
    val comments: List<Comment> = emptyList()
)
