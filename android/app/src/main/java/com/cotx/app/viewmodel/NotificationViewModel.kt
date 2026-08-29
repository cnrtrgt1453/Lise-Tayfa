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

    fun loadNotifications(userId: String) {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            _uiState.value = NotificationUiState.Loading
            repository.getUserNotifications(userId)
                .onSuccess { list ->
                    _uiState.value = NotificationUiState.Success(list)
                    _unreadCount.value = list.count { !it.isRead }
                }
                .onFailure { error ->
                    _uiState.value = NotificationUiState.Error(error.localizedMessage ?: "Bildirimler alınamadı.")
                }
        }
    }

    fun markAsRead(notificationId: String, userId: String) {
        _unreadCount.value = maxOf(0, _unreadCount.value - 1)
        viewModelScope.launch {
            repository.markAsRead(notificationId)
            loadNotifications(userId)
        }
    }

    fun markAllAsRead(userId: String) {
        if (userId.isEmpty()) return
        _unreadCount.value = 0
        viewModelScope.launch {
            repository.markAllAsRead(userId)
            loadNotifications(userId)
        }
    }
}

