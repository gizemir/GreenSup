package com.gizemir.plantapp.presentation.notification

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gizemir.plantapp.domain.model.notification.Notification
import com.gizemir.plantapp.domain.model.notification.NotificationType
import com.gizemir.plantapp.domain.repository.auth.AuthRepository
import com.gizemir.plantapp.domain.repository.notification.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.Date

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    // unreadNotificationCount alias for MainScaffold compatibility
    val unreadNotificationCount: StateFlow<Int> = _unreadCount

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    // Panel visibility state
    private val _showPanel = MutableStateFlow(false)
    val showPanel: StateFlow<Boolean> = _showPanel

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                Log.d("NotificationViewModel", "Loading current user...")
                val currentUser = authRepository.getCurrentUser()
                _currentUserId.value = currentUser?.id
                Log.d("NotificationViewModel", "Current user ID: ${currentUser?.id}")
                currentUser?.id?.let { userId ->
                    loadNotifications(userId)
                    loadUnreadCount(userId)
                } ?: run {
                    Log.w("NotificationViewModel", "No current user found")
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Error loading current user: ${e.message}")
                _isLoading.value = false
            }
        }
    }

    private fun loadNotifications(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d("NotificationViewModel", "Loading notifications for user: $userId")
            try {
                notificationRepository.getNotificationsForUser(userId).collect { notifications ->
                    Log.d("NotificationViewModel", "Received ${notifications.size} notifications")
                    _notifications.value = notifications
                    _unreadCount.value = notifications.count { !it.isRead }
                    if (_isLoading.value) {
                        _isLoading.value = false
                        Log.d("NotificationViewModel", "Loading completed")
                    }
                }
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Error loading notifications: ${e.message}")
                _isLoading.value = false
            }
        }
    }

    private fun loadUnreadCount(userId: String) {
        viewModelScope.launch {
            try {
                val count = notificationRepository.getUnreadCount(userId)
                _unreadCount.value = count
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Error loading unread count: ${e.message}")
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.markAsRead(notificationId)
                _notifications.value = _notifications.value.map { notification ->
                    if (notification.id == notificationId) {
                        notification.copy(isRead = true)
                    } else {
                        notification
                    }
                }
                _unreadCount.value = _notifications.value.count { !it.isRead }
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Error marking notification as read: ${e.message}")
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                _currentUserId.value?.let { userId ->
                    notificationRepository.markAllAsRead(userId)
                    _notifications.value = _notifications.value.map { it.copy(isRead = true) }
                    _unreadCount.value = 0
                }
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Error marking all notifications as read: ${e.message}")
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.deleteNotification(notificationId)
                _notifications.value = _notifications.value.filter { it.id != notificationId }
                _unreadCount.value = _notifications.value.count { !it.isRead }
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Error deleting notification: ${e.message}")
            }
        }
    }

    fun refreshNotifications() {
        _currentUserId.value?.let { userId ->
            loadNotifications(userId)
        }
    }

    fun togglePanel() {
        _showPanel.value = !_showPanel.value
    }

    fun closePanel() {
        _showPanel.value = false
    }
} 