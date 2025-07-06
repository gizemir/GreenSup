package com.gizemir.plantapp.data.repository.notification

import android.util.Log
import com.gizemir.plantapp.domain.model.notification.Notification
import com.gizemir.plantapp.domain.repository.notification.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : NotificationRepository {

    private val notificationsCollection = firestore.collection("notifications")

    override suspend fun createNotification(notification: Notification) {
        try {
            notificationsCollection.add(notification).await()
            Log.d("NotificationRepository", "Notification created: ${notification.type}")
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error creating notification: ${e.message}")
        }
    }

    override suspend fun getNotificationsForUser(userId: String): Flow<List<Notification>> = callbackFlow {
        val subscription = try {
            notificationsCollection
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        Log.e("NotificationRepository", "Error fetching notifications", exception)
                        return@addSnapshotListener
                    }
                    
                    val notifications = snapshot?.documents?.mapNotNull { document ->
                        try {
                            document.toObject(Notification::class.java)?.copy(id = document.id)
                        } catch (e: Exception) {
                            Log.e("NotificationRepository", "Error parsing notification: ${e.message}")
                            null
                        }
                    } ?: emptyList()
                    
                    trySend(notifications)
                }
        } catch (indexException: Exception) {
            Log.w("NotificationRepository", "Index not available, using fallback query: ${indexException.message}")
            notificationsCollection
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        Log.e("NotificationRepository", "Error fetching notifications (fallback)", exception)
                        return@addSnapshotListener
                    }
                    
                    val notifications = snapshot?.documents?.mapNotNull { document ->
                        try {
                            document.toObject(Notification::class.java)?.copy(id = document.id)
                        } catch (e: Exception) {
                            Log.e("NotificationRepository", "Error parsing notification: ${e.message}")
                            null
                        }
                    }?.sortedByDescending { it.timestamp } ?: emptyList()
                    
                    trySend(notifications)
                }
        }
            
        awaitClose { subscription.remove() }
    }

    override suspend fun markAsRead(notificationId: String) {
        try {
            notificationsCollection.document(notificationId)
                .update("isRead", true)
                .await()
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error marking notification as read: ${e.message}")
        }
    }

    override suspend fun markAllAsRead(userId: String) {
        try {
            val notifications = notificationsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()
                
            val batch = firestore.batch()
            notifications.documents.forEach { document ->
                batch.update(document.reference, "isRead", true)
            }
            batch.commit().await()
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error marking all notifications as read: ${e.message}")
        }
    }

    override suspend fun deleteNotification(notificationId: String) {
        try {
            notificationsCollection.document(notificationId).delete().await()
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error deleting notification: ${e.message}")
        }
    }

    override suspend fun getUnreadCount(userId: String): Int {
        return try {
            val snapshot = notificationsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error getting unread count: ${e.message}")
            0
        }
    }
} 