package com.cotx.app.data.repository

import com.cotx.app.data.model.ChatConversation
import com.cotx.app.data.model.ChatMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID

class ChatRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    /**
     * Listen for Global Chat messages (Top 50 most recent)
     */
    fun listenGlobalMessages(onUpdate: (List<ChatMessage>) -> Unit): ListenerRegistration {
        return firestore.collection("global_chat")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val messages = snapshot.toObjects(ChatMessage::class.java).reversed()
                onUpdate(messages)
            }
    }

    /**
     * Send a message to Global Chat and trim any messages beyond top 50
     */
    suspend fun sendGlobalMessage(
        senderId: String,
        senderName: String,
        senderPhotoUrl: String,
        text: String
    ): Result<Unit> = runCatching {
        if (text.isBlank()) return@runCatching

        val id = UUID.randomUUID().toString()
        val message = ChatMessage(
            id = id,
            senderId = senderId,
            senderName = senderName,
            senderPhotoUrl = senderPhotoUrl,
            text = text.trim(),
            isGlobal = true,
            createdAt = Date()
        )

        firestore.collection("global_chat").document(id).set(message).await()

        // Clean up messages beyond 50
        cleanupGlobalChatMessages()
    }

    /**
     * Clean up messages in global_chat keeping only the 50 most recent
     */
    suspend fun cleanupGlobalChatMessages(): Result<Unit> = runCatching {
        val snapshot = firestore.collection("global_chat")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await()

        val documents = snapshot.documents
        if (documents.size > 50) {
            val toDelete = documents.subList(50, documents.size)
            val batch = firestore.batch()
            toDelete.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
        }
    }

    /**
     * Compute deterministic 1-on-1 private room ID for 2 users
     */
    fun getRoomId(userId1: String, userId2: String): String {
        return listOf(userId1, userId2).sorted().joinToString("_")
    }

    /**
     * Purges private messages older than 5 days physically from Firestore
     */
    suspend fun cleanupExpiredPrivateMessages(roomId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (roomId.isEmpty()) return@runCatching
            val fiveDaysInMillis = 5L * 24 * 60 * 60 * 1000
            val cutoffDate = Date(System.currentTimeMillis() - fiveDaysInMillis)

            val expiredSnapshot = firestore.collection("chat_rooms").document(roomId)
                .collection("messages")
                .whereLessThan("createdAt", cutoffDate)
                .get().await()

            if (expiredSnapshot.isEmpty) return@runCatching

            val batch = firestore.batch()
            for (doc in expiredSnapshot.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().await()
        }
    }

    /**
     * Listen for private 1-on-1 messages in a room (keeping only last 5 days of messages)
     */
    fun listenPrivateMessages(roomId: String, onUpdate: (List<ChatMessage>) -> Unit): ListenerRegistration {
        val fiveDaysInMillis = 5L * 24 * 60 * 60 * 1000

        // Trigger background cleanup of messages older than 5 days
        CoroutineScope(Dispatchers.IO).launch {
            cleanupExpiredPrivateMessages(roomId)
        }

        return firestore.collection("chat_rooms").document(roomId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val cutoff = Date(System.currentTimeMillis() - fiveDaysInMillis)
                val messages = snapshot.toObjects(ChatMessage::class.java).filter { msg ->
                    msg.createdAt == null || msg.createdAt >= cutoff
                }
                onUpdate(messages)
            }
    }

    /**
     * Send a private 1-on-1 message between 2 users
     */
    suspend fun sendPrivateMessage(
        senderId: String,
        senderName: String,
        senderPhotoUrl: String,
        receiverId: String,
        receiverName: String,
        receiverPhotoUrl: String,
        text: String
    ): Result<Unit> = runCatching {
        if (text.isBlank() || receiverId.isEmpty()) return@runCatching

        val roomId = getRoomId(senderId, receiverId)
        val messageId = UUID.randomUUID().toString()
        val now = Date()

        val message = ChatMessage(
            id = messageId,
            senderId = senderId,
            senderName = senderName,
            senderPhotoUrl = senderPhotoUrl,
            text = text.trim(),
            isGlobal = false,
            chatRoomId = roomId,
            createdAt = now
        )

        // Save message in chat room
        firestore.collection("chat_rooms").document(roomId)
            .collection("messages").document(messageId)
            .set(message).await()

        // Update sender's conversation list (sender has read their own sent message)
        val senderConv = ChatConversation(
            chatRoomId = roomId,
            otherUserId = receiverId,
            otherUserName = receiverName,
            otherUserPhotoUrl = receiverPhotoUrl,
            lastMessage = text.trim(),
            isRead = true,
            lastMessageTime = now
        )
        firestore.collection("users").document(senderId)
            .collection("conversations").document(roomId)
            .set(senderConv).await()

        // Update receiver's conversation list (receiver hasn't read it yet -> isRead = false)
        val receiverConv = ChatConversation(
            chatRoomId = roomId,
            otherUserId = senderId,
            otherUserName = senderName,
            otherUserPhotoUrl = senderPhotoUrl,
            lastMessage = text.trim(),
            isRead = false,
            lastMessageTime = now
        )
        firestore.collection("users").document(receiverId)
            .collection("conversations").document(roomId)
            .set(receiverConv).await()

        // Trigger cleanup of older messages
        CoroutineScope(Dispatchers.IO).launch {
            cleanupExpiredPrivateMessages(roomId)
        }
    }

    /**
     * Mark a specific 1-on-1 conversation as read
     */
    suspend fun markConversationAsRead(userId: String, chatRoomId: String): Result<Unit> = runCatching {
        if (userId.isEmpty() || chatRoomId.isEmpty()) return@runCatching
        firestore.collection("users").document(userId)
            .collection("conversations").document(chatRoomId)
            .update("isRead", true).await()
    }

    /**
     * Mark all conversations for a user as read
     */
    suspend fun markAllConversationsAsRead(userId: String): Result<Unit> = runCatching {
        if (userId.isEmpty()) return@runCatching
        val snapshot = firestore.collection("users").document(userId)
            .collection("conversations")
            .whereEqualTo("isRead", false)
            .get().await()

        if (snapshot.isEmpty) return@runCatching

        val batch = firestore.batch()
        for (doc in snapshot.documents) {
            batch.update(doc.reference, "isRead", true)
        }
        batch.commit().await()
    }

    /**
     * Clear all global chat messages in Firestore
     */
    suspend fun clearGlobalChat(): Result<Unit> = runCatching {
        val snapshot = firestore.collection("global_chat").get().await()
        if (snapshot.isEmpty) return@runCatching
        val batch = firestore.batch()
        for (doc in snapshot.documents) {
            batch.delete(doc.reference)
        }
        batch.commit().await()
    }

    /**
     * Delete a single private message from a room
     */
    suspend fun deletePrivateMessage(roomId: String, messageId: String): Result<Unit> = runCatching {
        if (roomId.isEmpty() || messageId.isEmpty()) return@runCatching
        firestore.collection("chat_rooms").document(roomId)
            .collection("messages").document(messageId)
            .delete().await()
    }

    /**
     * Delete a conversation entry for a user
     */
    suspend fun deleteConversation(userId: String, chatRoomId: String): Result<Unit> = runCatching {
        if (userId.isEmpty() || chatRoomId.isEmpty()) return@runCatching
        firestore.collection("users").document(userId)
            .collection("conversations").document(chatRoomId)
            .delete().await()
    }

    /**
     * Listen for all private conversations of a user (keeping only conversations from last 5 days)
     */
    fun listenUserConversations(userId: String, onUpdate: (List<ChatConversation>) -> Unit): ListenerRegistration {
        val fiveDaysInMillis = 5L * 24 * 60 * 60 * 1000

        return firestore.collection("users").document(userId)
            .collection("conversations")
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val cutoff = Date(System.currentTimeMillis() - fiveDaysInMillis)
                val conversations = snapshot.toObjects(ChatConversation::class.java).filter { conv ->
                    conv.lastMessageTime == null || conv.lastMessageTime >= cutoff
                }
                onUpdate(conversations)
            }
    }
}
