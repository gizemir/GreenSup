package com.gizemir.plantapp.domain.model.forum

import java.util.Date
import java.util.UUID

data class Comment(
    val id: String = UUID.randomUUID().toString(),
    val postId: String,
    val userId: String,
    val userName: String,
    val userProfilePicUrl: String? = null,
    val content: String,
    val timestamp: Date = Date()
)
