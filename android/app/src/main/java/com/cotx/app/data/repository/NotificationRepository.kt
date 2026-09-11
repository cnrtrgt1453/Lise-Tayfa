package com.cotx.app.data.repository

import com.cotx.app.data.model.Notification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.UUID

class NotificationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    /**
     * Send a notification to recipient user
     */
    suspend fun sendNotification(
        userId: String,
        senderId: String,
        senderName: String,
        senderPhotoUrl: String,
        questionId: String,
        type: String,
        message: String
    ): Result<Unit> = runCatching {
        // Do not send notification to self
        if (userId.isEmpty() || userId == senderId) return@runCatching

        val id = UUID.randomUUID().toString()
        val notification = Notification(
            id = id,
            userId = userId,
            senderId = senderId,
            senderName = senderName,
            senderPhotoUrl = senderPhotoUrl,
            questionId = questionId,
            type = type,
            message = message,
            isRead = false,
            createdAt = java.util.Date()
        )

        firestore.collection("notifications").document(id).set(notification).await()
        Unit
    }.onFailure { e ->
        android.util.Log.e("NotificationRepository", "sendNotification failed to recipient $userId: ${e.message}", e)
    }

    /**
     * Fetch notifications for a user sorted by date descending
     */
    suspend fun getUserNotifications(userId: String): Result<List<Notification>> = runCatching {
        if (userId.isEmpty()) return@runCatching emptyList()

        val snapshot = firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .limit(50)
            .get().await()

        snapshot.toObjects(Notification::class.java)
            .sortedByDescending { it.createdAt }
    }

    /**
     * Mark notification as read
     */
    suspend fun markAsRead(notificationId: String): Result<Unit> = runCatching {
        firestore.collection("notifications").document(notificationId)
            .update(
                mapOf(
                    "isRead" to true,
                    "read" to true
                )
            ).await()
        Unit
    }

    /**
     * Mark all unread notifications for user as read
     */
    suspend fun markAllAsRead(userId: String): Result<Unit> = runCatching {
        if (userId.isEmpty()) return@runCatching
        val snapshot = firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .get().await()

        val unreadDocs = snapshot.documents.filter { doc ->
            val isReadVal = doc.getBoolean("isRead") ?: doc.getBoolean("read") ?: false
            !isReadVal
        }

        if (unreadDocs.isEmpty()) return@runCatching

        val batch = firestore.batch()
        for (doc in unreadDocs) {
            batch.update(doc.reference, mapOf("isRead" to true, "read" to true))
        }
        batch.commit().await()
        Unit
    }

    /**
     * Real-time listener for user notifications
     */
    fun listenUserNotifications(
        userId: String,
        onUpdate: (List<Notification>) -> Unit
    ): com.google.firebase.firestore.ListenerRegistration? {
        if (userId.isEmpty()) return null
        return firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("NotificationRepository", "listenUserNotifications error: ${error.message}", error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                val notifications = snapshot.toObjects(Notification::class.java)
                    .sortedByDescending { it.createdAt }
                onUpdate(notifications)
            }
    }

    /**
     * Update FCM device token for push notifications
     */
    suspend fun updateFcmToken(userId: String, token: String): Result<Unit> = runCatching {
        if (userId.isEmpty() || token.isEmpty()) return@runCatching
        firestore.collection("users").document(userId)
            .update("fcmToken", token)
            .await()
        Unit
    }
}

