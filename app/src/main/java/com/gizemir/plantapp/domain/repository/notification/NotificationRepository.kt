package com.gizemir.plantapp.domain.repository.notification

import com.gizemir.plantapp.domain.model.notification.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun createNotification(notification: Notification)
    suspend fun getNotificationsForUser(userId: String): Flow<List<Notification>>
    suspend fun markAsRead(notificationId: String)
    suspend fun markAllAsRead(userId: String)
    suspend fun deleteNotification(notificationId: String)
    suspend fun getUnreadCount(userId: String): Int
} 