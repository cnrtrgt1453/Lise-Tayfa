package com.cotx.app.data.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ChatConversation(
    val chatRoomId: String = "",
    val otherUserId: String = "",
    val otherUserName: String = "",
    val otherUserPhotoUrl: String = "",
    val lastMessage: String = "",
    @get:PropertyName("isRead") @set:PropertyName("isRead") @field:JvmField
    var isRead: Boolean = true,
    @ServerTimestamp
    val lastMessageTime: Date? = null
)
