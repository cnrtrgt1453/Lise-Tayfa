package com.cotx.app.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderPhotoUrl: String = "",
    val text: String = "",
    val isGlobal: Boolean = false,
    val chatRoomId: String = "",
    @ServerTimestamp
    val createdAt: Date? = null
)
