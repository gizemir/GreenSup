package com.gizemir.plantapp.data.remote.dto.forum

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class CommentDto(
    @DocumentId
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userProfilePicUrl: String? = null,
    val content: String = "",
    @ServerTimestamp
    val timestamp: Date = Date()
)
