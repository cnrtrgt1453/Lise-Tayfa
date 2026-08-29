package com.cotx.app.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class DirectMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    @ServerTimestamp
    val timestamp: Date? = null
)
