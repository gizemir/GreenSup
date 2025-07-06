package com.gizemir.plantapp.domain.model.notification

import java.util.Date
import java.util.UUID

data class Notification(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val type: NotificationType = NotificationType.COMMENT,
    val title: String = "",
    val message: String = "",
    val postId: String? = null,
    val commentId: String? = null,
    val fromUserId: String = "",
    val fromUserName: String = "",
    val fromUserProfilePic: String? = null,
    val timestamp: Date = Date(),
    val isRead: Boolean = false
)

enum class NotificationType {
    COMMENT,
    LIKE,
    REPLY,
    WATERING
} 