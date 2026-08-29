package com.cotx.app.data.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Notification(
    val id: String = "",
    val userId: String = "", // Alıcı Kullanıcı UID
    val senderId: String = "", // Gönderen Kullanıcı UID
    val senderName: String = "",
    val senderPhotoUrl: String = "",
    val questionId: String = "",
    val type: String = "", // "LIKE" veya "COMMENT"
    val message: String = "",
    @get:PropertyName("isRead") @set:PropertyName("isRead") @field:JvmField
    var isRead: Boolean = false,
    @ServerTimestamp
    val createdAt: Date? = null
)
