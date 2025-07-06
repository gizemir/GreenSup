package com.gizemir.plantapp.data.remote.dto.forum

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class LikeDto(
    @DocumentId
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    @ServerTimestamp
    val timestamp: Date = Date()
)
