package com.cotx.app.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val bio: String = "",
    val examType: String = "", // "TYT/AYT" or "LGS"
    val field: String = "Sayısal", // Sayısal, Eşit Ağırlık, Sözel, Dil
    val targetUniversity: String = "",
    val targetMajor: String = "",
    val gradeLevel: String = "12. Sınıf", // 9, 10, 11, 12, Mezun
    val badges: List<String> = emptyList(), // Örn: "Matematik Canavarı", "Topluluk Lideri"
    val following: List<String> = emptyList(), // Takip ettiği kullanıcıların UID'leri
    val followers: List<String> = emptyList(), // Takip eden kullanıcıların UID'leri
    val visibleBadges: List<String>? = null, // Profilde gösterilmesi seçilen rozetler (null ise varsayılan, [] ise hiç gösterilmez)
    val blockedUsers: List<String> = emptyList(), // Engellenen kullanıcıların UID'leri
    val subjectCounts: Map<String, Int> = emptyMap(), // Ders bazlı soru sayıları (Matematik -> 35, Fizik -> 10)
    val isPro: Boolean = false,
    val solvedCount: Int = 0,
    val askedCount: Int = 0,
    val fcmToken: String = "",
    @ServerTimestamp
    val createdAt: Date? = null
)
