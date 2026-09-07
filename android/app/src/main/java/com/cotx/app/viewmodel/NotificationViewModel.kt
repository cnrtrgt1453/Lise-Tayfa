package com.cotx.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cotx.app.data.model.Notification
import com.cotx.app.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface NotificationUiState {
    object Loading : NotificationUiState
    data class Success(val notifications: List<Notification>) : NotificationUiState
    data class Error(val message: String) : NotificationUiState
}

class NotificationViewModel(
    private val repository: NotificationRepository = NotificationRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Loading)
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var activeUserId: String? = null
    private var knownNotificationIds = mutableSetOf<String>()
    private var isInitialSnapshot = true

    fun initRealtimeListener(userId: String, context: android.content.Context? = null) {
        if (userId.isEmpty()) return
        if (activeUserId == userId && listenerRegistration != null) return

        listenerRegistration?.remove()
        activeUserId = userId
        isInitialSnapshot = true
        knownNotificationIds.clear()

        _uiState.value = NotificationUiState.Loading

        listenerRegistration = repository.listenUserNotifications(userId) { list ->
            val unread = list.count { !it.isRead }
            _unreadCount.value = unread
            _uiState.value = NotificationUiState.Success(list)

            // If a new unread notification arrived after initial load, show a system notification
            if (!isInitialSnapshot && context != null) {
                val newUnreadNotifications = list.filter { !it.isRead && !knownNotificationIds.contains(it.id) }
                for (notif in newUnreadNotifications) {
                    com.cotx.app.util.NotificationHelper.showNotification(
                        context = context.applicationContext,
                        title = "cotx • " + when (notif.type) {
                            "LIKE" -> "Beğeni ❤️"
                            "COMMENT" -> "Yeni Çözüm / Yorum 💬"
                            "FOLLOW" -> "Yeni Takipçi 👤"
                            "NEW_QUESTION" -> "Yeni Soru 🚀"
                            else -> "Bildirim 🔔"
                        },
                        message = notif.message,
                        questionId = notif.questionId,
                        notificationType = notif.type
                    )
                }
            }

            knownNotificationIds.clear()
            knownNotificationIds.addAll(list.map { it.id })
            isInitialSnapshot = false
        }
    }

    fun loadNotifications(userId: String) {
        if (userId.isEmpty()) return
        initRealtimeListener(userId)
    }

    fun markAsRead(notificationId: String, userId: String) {
        _unreadCount.value = maxOf(0, _unreadCount.value - 1)
        viewModelScope.launch {
            repository.markAsRead(notificationId)
        }
    }

    fun markAllAsRead(userId: String) {
        if (userId.isEmpty()) return
        _unreadCount.value = 0
        viewModelScope.launch {
            repository.markAllAsRead(userId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
        listenerRegistration = null
    }
}

