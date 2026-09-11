package com.cotx.app.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Solution(
    val id: String = "",
    val questionId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String = "",
    val contentText: String = "",
    val solutionImageUrl: String? = null,
    val isAcceptedAnswer: Boolean = false,
    val upvotes: Int = 0,
    val likeCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    val replyToSolutionId: String? = null,
    val replyToAuthorName: String? = null,
    @ServerTimestamp
    val createdAt: Date? = null
)
