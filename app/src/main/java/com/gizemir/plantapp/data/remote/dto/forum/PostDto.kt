package com.gizemir.plantapp.data.remote.dto.forum

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class PostDto(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userProfilePicUrl: String? = null,
    val content: String = "",
    val imageUrl: String? = null,
    @ServerTimestamp
    val timestamp: Date = Date(),
    val likeCount: Int = 0,
    val commentCount: Int = 0
)
