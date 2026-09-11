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
        Unit
    }.onFailure { e ->
        android.util.Log.e("QuestionRepository", "toggleLikeQuestion error: ${e.message}", e)
    }

    /**
     * Toggle like for a solution/comment
     */
    suspend fun toggleLikeSolution(
        questionId: String,
        solutionId: String,
        userId: String,
        userName: String = "",
        userPhotoUrl: String = "",
        isLiked: Boolean
    ): Result<Unit> = runCatching {
        val solDocRef = firestore.collection("questions").document(questionId)
            .collection("solutions").document(solutionId)

        if (isLiked) {
            solDocRef.update(
                "likeCount", FieldValue.increment(-1),
                "likedBy", FieldValue.arrayRemove(userId)
            ).await()
        } else {
            solDocRef.update(
                "likeCount", FieldValue.increment(1),
                "likedBy", FieldValue.arrayUnion(userId)
            ).await()

            // Trigger notification to solution author
            runCatching {
                val solSnapshot = solDocRef.get().await()
                val targetAuthorId = solSnapshot.getString("authorId")
                if (!targetAuthorId.isNullOrEmpty() && targetAuthorId != userId) {
                    notificationRepository.sendNotification(
                        userId = targetAuthorId,
                        senderId = userId,
                        senderName = userName.ifEmpty { "Bir öğrenci" },
                        senderPhotoUrl = userPhotoUrl,
                        questionId = questionId,
                        type = "LIKE",
                        message = "${userName.ifEmpty { "Bir öğrenci" }} çözümünü/yorumunu beğendi ❤️"
                    )
                }
            }.onFailure { e ->
                android.util.Log.e("QuestionRepository", "Failed to send like notification for solution: ${e.message}", e)
            }
        }
        Unit
    }.onFailure { e ->
        android.util.Log.e("QuestionRepository", "toggleLikeSolution error: ${e.message}", e)
    }

    /**
     * Upload solution image with WebP compression and fallback
     */
    private suspend fun uploadSolutionImage(
        context: Context,
        solutionId: String,
        imageUri: Uri
    ): String? = withContext(Dispatchers.IO) {
        val imageBytes = ImageCompressor.compressUriToWebp(context, imageUri)
        if (imageBytes.isEmpty()) return@withContext null

        var downloadUrl: String? = null
        val metadata = StorageMetadata.Builder()
            .setContentType("image/jpeg")
            .build()

        val bucketCandidates = listOf(
            "gs://cotx-c167c.appspot.com",
            "gs://cotx-c167c.firebasestorage.app",
            null
        )

        for (bucketUrl in bucketCandidates) {
            try {
                val storageInstance = if (bucketUrl != null) FirebaseStorage.getInstance(bucketUrl) else FirebaseStorage.getInstance()
                val storageRef = storageInstance.reference.child("solutions/$solutionId.jpg")
                val snapshot = storageRef.putBytes(imageBytes, metadata).await()
                downloadUrl = try {
                    storageRef.downloadUrl.await().toString()
                } catch (e: Exception) {
                    val bucket = snapshot.storage.bucket
                    val encodedPath = java.net.URLEncoder.encode("solutions/$solutionId.jpg", "UTF-8")
                    "https://firebasestorage.googleapis.com/v0/b/$bucket/o/$encodedPath?alt=media"
                }
                if (!downloadUrl.isNullOrEmpty()) break
            } catch (_: Exception) {}
        }

        if (!downloadUrl.isNullOrEmpty()) {
            downloadUrl
        } else {
            val base64Str = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
            "data:image/jpeg;base64,$base64Str"
        }
    }

    /**
     * Post a solution / comment with optional photo and reply support
     */
    suspend fun addSolution(
        questionId: String,
        authorId: String,
        authorName: String,
        authorPhotoUrl: String,
        contentText: String,
        replyToSolutionId: String? = null,
        replyToAuthorName: String? = null,
        imageUri: Uri? = null,
        context: Context? = null
    ): Result<Solution> = runCatching {
        if (contentText.isNotBlank() && com.cotx.app.util.ProfanityFilter.containsProfanity(contentText)) {
            throw IllegalArgumentException("Yorumunuz topluluk kurallarına aykırı ifadeler (argo/küfür/hakaret) içermektedir.")
        }

        val solutionId = UUID.randomUUID().toString()

        var uploadedImageUrl: String? = null
        if (imageUri != null && context != null) {
            uploadedImageUrl = uploadSolutionImage(context, solutionId, imageUri)
        }

        val solution = Solution(
            id = solutionId,
            questionId = questionId,
            authorId = authorId,
            authorName = authorName,
            authorPhotoUrl = authorPhotoUrl,
            contentText = contentText.trim(),
            solutionImageUrl = uploadedImageUrl,
            replyToSolutionId = replyToSolutionId,
            replyToAuthorName = replyToAuthorName
        )

        firestore.collection("questions").document(questionId)
            .collection("solutions").document(solutionId)
            .set(solution).await()

        // Safely increment commentCount without blocking notification flow
        runCatching {
            firestore.collection("questions").document(questionId)
                .update("commentCount", FieldValue.increment(1)).await()
        }.onFailure { e ->
            android.util.Log.w("QuestionRepository", "Failed to increment commentCount: ${e.message}", e)
        }

        // Trigger notifications according to requirements:
        // 1. If this is a reply to another comment: ONLY notify the author of the replied comment
        // 2. If this is a normal comment: ONLY notify the question author (do NOT notify previous commenters)
        runCatching {
            if (!replyToSolutionId.isNullOrEmpty()) {
                val repliedSolDoc = firestore.collection("questions").document(questionId)
                    .collection("solutions").document(replyToSolutionId)
                    .get().await()
                val targetAuthorId = repliedSolDoc.getString("authorId")
                if (!targetAuthorId.isNullOrEmpty() && targetAuthorId != authorId) {
                    val snippet = if (contentText.length > 35) contentText.take(35) + "..." else contentText
                    val notifMessage = if (snippet.isNotEmpty()) {
                        "$authorName yorumunu yanıtladı: \"$snippet\" 💬"
                    } else {
                        "$authorName yorumuna fotoğrafla yanıt verdi 📷"
                    }
                    notificationRepository.sendNotification(
                        userId = targetAuthorId,
                        senderId = authorId,
                        senderName = authorName,
                        senderPhotoUrl = authorPhotoUrl,
                        questionId = questionId,
                        type = "COMMENT",
                        message = notifMessage
                    )
                }
            } else {
                val qSnapshot = firestore.collection("questions").document(questionId).get().await()
                val question = qSnapshot.toObject(Question::class.java)
                if (question != null && question.authorId.isNotEmpty() && question.authorId != authorId) {
                    val notifMessage = if (contentText.isNotEmpty()) {
                        "$authorName soruna yeni bir çözüm ekledi 💬"
                    } else {
                        "$authorName soruna fotoğraflı çözüm ekledi 📷"
                    }
                    notificationRepository.sendNotification(
                        userId = question.authorId,
                        senderId = authorId,
                        senderName = authorName,
                        senderPhotoUrl = authorPhotoUrl,
                        questionId = questionId,
                        type = "COMMENT",
                        message = notifMessage
                    )
                }
            }
        }.onFailure { e ->
            android.util.Log.e("QuestionRepository", "Failed to trigger comment notifications: ${e.message}", e)
        }

        solution
    }.onFailure { e ->
        android.util.Log.e("QuestionRepository", "addSolution failed: ${e.message}", e)
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
        Unit
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
        Unit
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
        Unit
    }
}

