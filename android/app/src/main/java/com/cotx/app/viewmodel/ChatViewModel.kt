package com.cotx.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cotx.app.data.model.ChatConversation
import com.cotx.app.data.model.ChatMessage
import com.cotx.app.data.repository.ChatRepository
import com.cotx.app.domain.model.UserSummary
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class MessagesTab {
    GLOBAL_CHAT,
    PRIVATE_MESSAGES
}

class ChatViewModel(
    private val chatRepository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(MessagesTab.GLOBAL_CHAT)
    val selectedTab: StateFlow<MessagesTab> = _selectedTab.asStateFlow()

    private val _globalMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val globalMessages: StateFlow<List<ChatMessage>> = _globalMessages.asStateFlow()

    private val _conversations = MutableStateFlow<List<ChatConversation>>(emptyList())
    val conversations: StateFlow<List<ChatConversation>> = _conversations.asStateFlow()

    private val _unreadMessageCount = MutableStateFlow(0)
    val unreadMessageCount: StateFlow<Int> = _unreadMessageCount.asStateFlow()

    private var globalListener: ListenerRegistration? = null
    private var conversationsListener: ListenerRegistration? = null

    fun selectTab(tab: MessagesTab) {
        _selectedTab.value = tab
    }

    fun initListeners(userId: String) {
        if (userId.isEmpty()) return

        if (globalListener == null) {
            globalListener = chatRepository.listenGlobalMessages { messages ->
                _globalMessages.value = messages
            }
        }

        if (conversationsListener == null) {
            conversationsListener = chatRepository.listenUserConversations(userId) { list ->
                _conversations.value = list
                _unreadMessageCount.value = list.count { !it.isRead }
            }
        }
    }

    fun markConversationAsRead(userId: String, chatRoomId: String) {
        if (userId.isEmpty() || chatRoomId.isEmpty()) return
        viewModelScope.launch {
            chatRepository.markConversationAsRead(userId, chatRoomId)
        }
    }

    fun markAllConversationsAsRead(userId: String) {
        if (userId.isEmpty()) return
        _unreadMessageCount.value = 0
        viewModelScope.launch {
            chatRepository.markAllConversationsAsRead(userId)
        }
    }

    fun clearGlobalChat() {
        viewModelScope.launch {
            chatRepository.clearGlobalChat()
        }
    }

    fun deletePrivateMessage(roomId: String, messageId: String) {
        if (roomId.isEmpty() || messageId.isEmpty()) return
        viewModelScope.launch {
            chatRepository.deletePrivateMessage(roomId, messageId)
        }
    }

    fun deleteConversation(userId: String, chatRoomId: String) {
        if (userId.isEmpty() || chatRoomId.isEmpty()) return
        viewModelScope.launch {
            chatRepository.deleteConversation(userId, chatRoomId)
        }
    }

    fun sendGlobalMessage(currentUser: UserSummary, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            chatRepository.sendGlobalMessage(
                senderId = currentUser.id,
                senderName = currentUser.displayName.ifEmpty { "Öğrenci" },
                senderPhotoUrl = currentUser.avatarUrl ?: "",
                text = text
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        globalListener?.remove()
        conversationsListener?.remove()
    }
}
