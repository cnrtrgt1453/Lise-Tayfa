package com.cotx.app.data.repository

import android.content.Context
import android.net.Uri
import com.cotx.app.data.model.Question
import com.cotx.app.data.model.Solution
import com.cotx.app.util.ImageCompressor
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await
import java.util.UUID

import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuestionRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val notificationRepository: NotificationRepository = NotificationRepository()
) {

    /**
     * Uploads compressed WebP image to Cloud Storage and saves Question object in Firestore
     */
    suspend fun postQuestion(
        context: Context,
        authorId: String,
        authorName: String,
        authorPhotoUrl: String,
        examType: String = "TYT/AYT",
        subject: String,
        topic: String,
        description: String,
        mode: String,
        imageUri: Uri
    ): Result<Question> = withContext(Dispatchers.IO) {
        runCatching {
            val questionId = UUID.randomUUID().toString()

            // 1. Compress image to JPEG byte array safely
            val imageBytes = ImageCompressor.compressUriToWebp(context, imageUri)
            if (imageBytes.isEmpty()) {
                throw IllegalStateException("Fotoğraf işlenirken bir hata oluştu. Lütfen tekrar deneyin.")
            }

            // 2. Upload to Firebase Storage with multi-bucket detection
            var downloadUrl: String? = null
            var uploadError: Exception? = null

            val metadata = StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build()

            val bucketCandidates = listOf(
                "gs://cotx-c167c.appspot.com",
                "gs://cotx-c167c.firebasestorage.app",
                null // default
            )

            for (bucketUrl in bucketCandidates) {
                try {
                    val storageInstance = if (bucketUrl != null) {
                        FirebaseStorage.getInstance(bucketUrl)
                    } else {
                        FirebaseStorage.getInstance()
                    }
                    val storageRef = storageInstance.reference.child("questions/$questionId.jpg")
                    val snapshot = storageRef.putBytes(imageBytes, metadata).await()

                    downloadUrl = try {
                        storageRef.downloadUrl.await().toString()
                    } catch (e: Exception) {
                        val bucket = snapshot.storage.bucket
                        val encodedPath = java.net.URLEncoder.encode("questions/$questionId.jpg", "UTF-8")
                        "https://firebasestorage.googleapis.com/v0/b/$bucket/o/$encodedPath?alt=media"
                    }

                    if (!downloadUrl.isNullOrEmpty()) {
                        break
                    }
                } catch (e: Exception) {
                    uploadError = e
                }
            }

            // 3. Fallback to Base64 Data URI if Storage is not enabled or accessible
            val finalImageUrl = if (!downloadUrl.isNullOrEmpty()) {
                downloadUrl
            } else {
                val base64Str = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
                "data:image/jpeg;base64,$base64Str"
            }

            val now = java.util.Date()
            val threeDaysInMillis = 3L * 24 * 60 * 60 * 1000
            val expiresDate = java.util.Date(now.time + threeDaysInMillis)

            // 4. Save Question in Firestore
            val question = Question(
                id = questionId,
                authorId = authorId,
                authorName = authorName,
                authorPhotoUrl = authorPhotoUrl,
                imageUrl = finalImageUrl,
                examType = examType,
                subject = subject,
                topic = topic,
                title = topic,
                description = description,
                mode = mode,
                createdAt = now,
                expiresAt = expiresDate
            )

            firestore.collection("questions").document(questionId).set(question).await()

            // Update askedCount and subjectCounts in user's profile safely with merge
            if (authorId.isNotEmpty()) {
                runCatching {
                    firestore.collection("users").document(authorId).set(
                        mapOf(
                            "askedCount" to FieldValue.increment(1),
                            "subjectCounts" to mapOf(subject to FieldValue.increment(1))
                        ),
                        SetOptions.merge()
                    ).await()
                }

                // Send notifications to author's followers
                runCatching {
                    val authorDoc = firestore.collection("users").document(authorId).get().await()
                    @Suppress("UNCHECKED_CAST")
                    val followers = authorDoc.get("followers") as? List<String> ?: emptyList()

                    val notifMessage = if (mode.contains("Taktik", ignoreCase = true)) {
                        "$authorName $subject dersinde bir Taktik/Soru Tipi Paylaştı 🚀"
                    } else {
                        "$authorName $subject dersinde bir sorudan yardım istiyor"
                    }

                    for (followerId in followers) {
                        if (followerId.isNotEmpty() && followerId != authorId) {
                            notificationRepository.sendNotification(
                                userId = followerId,
                                senderId = authorId,
                                senderName = authorName,
                                senderPhotoUrl = authorPhotoUrl,
                                questionId = questionId,
                                type = "NEW_QUESTION",
                                message = notifMessage
                            )
                        }
                    }
                }
            }

            question
        }
    }

    /**
     * Purges expired questions (where expiresAt <= now) physically from Firestore database and Firebase Storage
     */
    suspend fun cleanupExpiredQuestions() = withContext(Dispatchers.IO) {
        runCatching {
            val now = java.util.Date()
            val expiredSnapshot = firestore.collection("questions")
                .whereLessThan("expiresAt", now)
                .limit(50)
                .get().await()

            for (doc in expiredSnapshot.documents) {
                val qId = doc.id
                // 1. Delete solutions subcollection
                runCatching {
                    val solSnapshot = doc.reference.collection("solutions").get().await()
                    for (solDoc in solSnapshot.documents) {
                        solDoc.reference.delete().await()
                    }
                }
                // 2. Delete image from Storage
                runCatching {
                    storage.reference.child("questions/$qId.jpg").delete().await()
                }
                // 3. Delete main document from Firestore physically
                doc.reference.delete().await()
            }
        }
    }

    /**
     * Fetches questions for Feed with feed type (EXPLORE vs FOLLOWING), examType, subject filtering, and date sorting.
     */
    suspend fun getFeedQuestions(
        selectedSubject: String? = null,
        examType: String? = null,
        isFollowingOnly: Boolean = false,
        followingUserIds: List<String> = emptyList(),
        blockedUserIds: List<String> = emptyList(),
        sortAscending: Boolean = false
    ): Result<List<Question>> = runCatching {
        // Trigger background physical cleanup of expired questions from database & storage
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            cleanupExpiredQuestions()
        }

        var query: Query = firestore.collection("questions")
            .limit(100)

        if (!selectedSubject.isNullOrEmpty() && selectedSubject != "Tümü") {
            query = firestore.collection("questions")
                .whereEqualTo("subject", selectedSubject)
                .limit(100)
        }

        val now = java.util.Date()
        val snapshot = query.get().await()
        val filtered = snapshot.toObjects(Question::class.java)
            .filter { question ->
                val notExpired = question.expiresAt == null || question.expiresAt.after(now)
                val matchesExam = examType.isNullOrEmpty() || question.examType.isEmpty() || question.examType == examType
                val matchesFollowing = !isFollowingOnly || followingUserIds.contains(question.authorId)
                val notBlocked = !blockedUserIds.contains(question.authorId)
                notExpired && matchesExam && matchesFollowing && notBlocked
            }

        if (sortAscending) {
            filtered.sortedBy { it.createdAt }
        } else {
            filtered.sortedByDescending { it.createdAt }
        }
    }

    /**
     * Toggle like for a question
     */
    suspend fun toggleLikeQuestion(
        questionId: String,
        userId: String,
        userName: String = "",
        userPhotoUrl: String = "",
        isLiked: Boolean
    ): Result<Unit> = runCatching {
        val docRef = firestore.collection("questions").document(questionId)
        if (isLiked) {
            docRef.update(
                "likeCount", FieldValue.increment(-1),
                "likedBy", FieldValue.arrayRemove(userId)
            ).await()
        } else {
            docRef.update(
                "likeCount", FieldValue.increment(1),
                "likedBy", FieldValue.arrayUnion(userId)
            ).await()

            // Trigger notification to question author
            runCatching {
                val qSnapshot = docRef.get().await()
                val question = qSnapshot.toObject(Question::class.java)
                if (question != null && question.authorId != userId) {
                    notificationRepository.sendNotification(
                        userId = question.authorId,
                        senderId = userId,
                        senderName = userName.ifEmpty { "Bir öğrenci" },
                        senderPhotoUrl = userPhotoUrl,
                        questionId = questionId,
                        type = "LIKE",
                        message = "${userName.ifEmpty { "Bir öğrenci" }} sorunu beğendi ❤️"
                    )
                }
            }
        }
    }

    /**
     * Post a solution / comment
     */
    suspend fun addSolution(
        questionId: String,
        authorId: String,
        authorName: String,
        authorPhotoUrl: String,
        contentText: String
    ): Result<Solution> = runCatching {
        if (com.cotx.app.util.ProfanityFilter.containsProfanity(contentText)) {
            throw IllegalArgumentException("Yorumunuz topluluk kurallarına aykırı ifadeler (argo/küfür/hakaret) içermektedir.")
        }

        val solutionId = UUID.randomUUID().toString()
        val solution = Solution(
            id = solutionId,
            questionId = questionId,
            authorId = authorId,
            authorName = authorName,
            authorPhotoUrl = authorPhotoUrl,
            contentText = contentText
        )

        firestore.collection("questions").document(questionId)
            .collection("solutions").document(solutionId)
            .set(solution).await()

        firestore.collection("questions").document(questionId)
            .update("commentCount", FieldValue.increment(1)).await()

        // Trigger notifications to question author and prior commenters
        runCatching {
            val qSnapshot = firestore.collection("questions").document(questionId).get().await()
            val question = qSnapshot.toObject(Question::class.java)

            // 1. Notify question author if commenter is not the question author
            if (question != null && question.authorId.isNotEmpty() && question.authorId != authorId) {
                notificationRepository.sendNotification(
                    userId = question.authorId,
                    senderId = authorId,
                    senderName = authorName,
                    senderPhotoUrl = authorPhotoUrl,
                    questionId = questionId,
                    type = "COMMENT",
                    message = "$authorName soruna yeni bir çözüm ekledi 💬"
                )
            }

            // 2. Fetch previous commenters for this question and notify them
            val solutionsSnapshot = firestore.collection("questions").document(questionId)
                .collection("solutions")
                .get().await()

            val previousCommenterIds = solutionsSnapshot.documents
                .mapNotNull { it.getString("authorId") }
                .filter { it.isNotEmpty() && it != authorId && it != question?.authorId }
                .distinct()

            for (commenterId in previousCommenterIds) {
                notificationRepository.sendNotification(
                    userId = commenterId,
                    senderId = authorId,
                    senderName = authorName,
                    senderPhotoUrl = authorPhotoUrl,
                    questionId = questionId,
                    type = "COMMENT",
                    message = "$authorName yorum yaptığın soruya yeni bir yorum ekledi 💬"
                )
            }
        }

        solution
    }

    /**
     * Get solutions for a specific question
     */
    suspend fun getQuestionSolutions(questionId: String): Result<List<Solution>> = runCatching {
        val snapshot = firestore.collection("questions").document(questionId)
            .collection("solutions")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .get().await()

        snapshot.toObjects(Solution::class.java)
    }

    /**
     * Get questions posted by a specific user
     */
    suspend fun getUserQuestions(userId: String): Result<List<Question>> = runCatching {
        val snapshot = firestore.collection("questions")
            .whereEqualTo("authorId", userId)
            .get().await()

        val now = java.util.Date()
        snapshot.toObjects(Question::class.java)
            .filter { question -> question.expiresAt == null || question.expiresAt.after(now) }
            .sortedByDescending { it.createdAt }
    }

    /**
     * Get single question by ID
     */
    suspend fun getQuestionById(questionId: String): Result<Question?> = runCatching {
        val doc = firestore.collection("questions").document(questionId).get().await()
        doc.toObject(Question::class.java)
    }

    /**
     * Deletes a question document from Firestore and removes image from Storage if available
     */
    suspend fun deleteQuestion(questionId: String, userId: String): Result<Unit> = runCatching {
        val docRef = firestore.collection("questions").document(questionId)
        val snapshot = docRef.get().await()
        val question = snapshot.toObject(Question::class.java) ?: throw Exception("Soru bulunamadı")

        if (question.authorId != userId) {
            throw IllegalAccessException("Bu soruyu silme yetkiniz yok")
        }

        // Delete main document
        docRef.delete().await()

        // Delete solutions subcollection
        runCatching {
            val solutionsSnapshot = docRef.collection("solutions").get().await()
            for (solDoc in solutionsSnapshot.documents) {
                solDoc.reference.delete().await()
            }
        }

        // Delete image from Storage if present
        runCatching {
            val storageRef = storage.reference.child("questions/$questionId.jpg")
            storageRef.delete().await()
        }

        // Decrement askedCount in user profile
        runCatching {
            firestore.collection("users").document(userId).set(
                mapOf("askedCount" to FieldValue.increment(-1)),
                SetOptions.merge()
            ).await()
        }
    }

    /**
     * Reports a question for violating community guidelines / UGC policy
     */
    suspend fun reportQuestion(
        questionId: String,
        reporterId: String,
        reason: String,
        note: String = ""
    ): Result<Unit> = runCatching {
        val reportId = UUID.randomUUID().toString()
        val reportData = mapOf(
            "id" to reportId,
            "targetId" to questionId,
            "reporterId" to reporterId,
            "type" to "QUESTION",
            "reason" to reason,
            "note" to note,
            "createdAt" to java.util.Date()
        )
        firestore.collection("reports").document(reportId).set(reportData).await()
    }

    /**
     * Reports a solution / comment for violating community guidelines
     */
    suspend fun reportSolution(
        questionId: String,
        solutionId: String,
        reporterId: String,
        reason: String,
        note: String = ""
    ): Result<Unit> = runCatching {
        val reportId = UUID.randomUUID().toString()
        val reportData = mapOf(
            "id" to reportId,
            "targetId" to solutionId,
            "questionId" to questionId,
            "reporterId" to reporterId,
            "type" to "SOLUTION",
            "reason" to reason,
            "note" to note,
            "createdAt" to java.util.Date()
        )
        firestore.collection("reports").document(reportId).set(reportData).await()
    }
}

