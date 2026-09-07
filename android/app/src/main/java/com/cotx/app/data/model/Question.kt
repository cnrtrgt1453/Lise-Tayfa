package com.cotx.app.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Question(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String = "",
    val imageUrl: String = "",
    val examType: String = "TYT/AYT", // "TYT/AYT" veya "LGS"
    val subject: String = "Matematik", // Matematik, Fizik, Kimya, Biyoloji, Türkçe, Tarih vb.
    val topic: String = "",
    val title: String = "",
    val description: String = "",
    val mode: String = "Yardım İstiyorum", // "Yardım İstiyorum" veya "Taktik / Soru Tipi"
    val isSolved: Boolean = false,
    val solvedSolutionId: String? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val likedBy: List<String> = emptyList(), // User UIDs
    @ServerTimestamp
    val createdAt: Date? = null,
    val expiresAt: Date? = null // 3 gün sonra süresi dolma tarihi
) {
    val displayTitle: String
        get() = title.ifEmpty { topic }
}
