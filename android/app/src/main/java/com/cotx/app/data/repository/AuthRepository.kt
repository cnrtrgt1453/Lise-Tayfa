package com.cotx.app.data.repository

import com.cotx.app.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

import com.google.firebase.firestore.FieldValue

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val notificationRepository: NotificationRepository = NotificationRepository()
) {

    val currentUser get() = auth.currentUser

    suspend fun registerUser(
        email: String,
        pass: String,
        displayName: String,
        field: String,
        targetUniversity: String,
        targetMajor: String,
        gradeLevel: String
    ): Result<User> = runCatching {
        val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
        val uid = authResult.user?.uid ?: throw Exception("User UID is null")

        val user = User(
            uid = uid,
            displayName = displayName,
            email = email,
            field = field,
            targetUniversity = targetUniversity,
            targetMajor = targetMajor,
            gradeLevel = gradeLevel,
            badges = listOf("Yeni Tayfa")
        )

        firestore.collection("users").document(uid).set(user).await()
        user
    }

    suspend fun loginUser(email: String, pass: String): Result<User> = runCatching {
        val authResult = auth.signInWithEmailAndPassword(email, pass).await()
        val uid = authResult.user?.uid ?: throw Exception("User UID is null")
        val doc = firestore.collection("users").document(uid).get().await()
        doc.toObject(User::class.java) ?: throw Exception("User data not found")
    }

    suspend fun loginWithGoogle(idToken: String): Result<User> = runCatching {
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
        val authResult = auth.signInWithCredential(credential).await()
        val firebaseUser = authResult.user ?: throw Exception("Google ile giriş başarısız.")
        val uid = firebaseUser.uid

        val userDoc = firestore.collection("users").document(uid).get().await()
        if (userDoc.exists()) {
            userDoc.toObject(User::class.java) ?: throw Exception("Kullanıcı profili alınamadı.")
        } else {
            val newUser = User(
                uid = uid,
                displayName = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "Öğrenci",
                email = firebaseUser.email ?: "",
                photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                field = "Sayısal",
                targetUniversity = "Hedef Belirlenmedi",
                targetMajor = "Hedef Belirlenmedi",
                gradeLevel = "YKS Tayfa",
                badges = listOf("Google Tayfası", "Yeni Tayfa")
            )
            firestore.collection("users").document(uid).set(newUser).await()
            newUser
        }
    }

    suspend fun getCurrentUserProfile(): Result<User?> = runCatching {
        val uid = currentUser?.uid ?: return@runCatching null
        val doc = firestore.collection("users").document(uid).get().await()
        doc.toObject(User::class.java)
    }

    suspend fun updateExamType(uid: String, examType: String): Result<Unit> = runCatching {
        firestore.collection("users").document(uid).update("examType", examType).await()
    }

    suspend fun updateUserProfile(
        context: android.content.Context,
        uid: String,
        displayName: String,
        bio: String,
        field: String,
        examType: String,
        targetUniversity: String,
        targetMajor: String,
        visibleBadges: List<String> = emptyList(),
        imageUri: android.net.Uri? = null
    ): Result<User> = runCatching {
        var photoUrl: String? = null
        if (imageUri != null) {
            val imageBytes = com.cotx.app.util.ImageCompressor.compressUriToWebp(context, imageUri)
            if (imageBytes.isNotEmpty()) {
                val metadata = com.google.firebase.storage.StorageMetadata.Builder()
                    .setContentType("image/jpeg")
                    .build()

                val bucketCandidates = listOf(
                    "gs://cotx-c167c.appspot.com",
                    "gs://cotx-c167c.firebasestorage.app",
                    null
                )

                var uploadedUrl: String? = null
                for (bucketUrl in bucketCandidates) {
                    try {
                        val storageInstance = if (bucketUrl != null) {
                            com.google.firebase.storage.FirebaseStorage.getInstance(bucketUrl)
                        } else {
                            com.google.firebase.storage.FirebaseStorage.getInstance()
                        }
                        val storageRef = storageInstance.reference.child("profile_photos/$uid.jpg")
                        val snapshot = storageRef.putBytes(imageBytes, metadata).await()

                        val rawUrl = try {
                            storageRef.downloadUrl.await().toString()
                        } catch (e: Exception) {
                            val bucket = snapshot.storage.bucket
                            val encodedPath = java.net.URLEncoder.encode("profile_photos/$uid.jpg", "UTF-8")
                            "https://firebasestorage.googleapis.com/v0/b/$bucket/o/$encodedPath?alt=media"
                        }

                        if (rawUrl.isNotEmpty()) {
                            uploadedUrl = if (rawUrl.contains("?")) {
                                "$rawUrl&t=${System.currentTimeMillis()}"
                            } else {
                                "$rawUrl?t=${System.currentTimeMillis()}"
                            }
                            break
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                if (!uploadedUrl.isNullOrEmpty()) {
                    photoUrl = uploadedUrl
                } else {
                    val base64Str = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
                    photoUrl = "data:image/jpeg;base64,$base64Str"
                }
            }
        }

        val updates = mutableMapOf<String, Any>(
            "displayName" to displayName,
            "bio" to bio,
            "field" to field,
            "examType" to examType,
            "targetUniversity" to targetUniversity,
            "targetMajor" to targetMajor,
            "visibleBadges" to visibleBadges
        )
        if (photoUrl != null) {
            updates["photoUrl"] = photoUrl
        }

        firestore.collection("users").document(uid).update(updates).await()

        val updatedDoc = firestore.collection("users").document(uid).get().await()
        updatedDoc.toObject(User::class.java) ?: throw Exception("Profil güncellenemedi")
    }

    suspend fun deleteUserAccount(uid: String): Result<Unit> = runCatching {
        firestore.collection("users").document(uid).delete().await()
        val firebaseUser = auth.currentUser
        firebaseUser?.delete()?.await()
        auth.signOut()
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun getUserProfile(userId: String): Result<User?> = runCatching {
        val doc = firestore.collection("users").document(userId).get().await()
        doc.toObject(User::class.java)
    }

    suspend fun getUsersByIds(userIds: List<String>): Result<List<User>> = runCatching {
        if (userIds.isEmpty()) return@runCatching emptyList()
        val chunked = userIds.chunked(10)
        val result = mutableListOf<User>()
        for (chunk in chunked) {
            val snapshot = firestore.collection("users")
                .whereIn("uid", chunk)
                .get().await()
            result.addAll(snapshot.toObjects(User::class.java))
        }
        result
    }

    suspend fun followUser(
        currentUserId: String,
        targetUserId: String,
        currentUserName: String = "",
        currentUserPhotoUrl: String = ""
    ): Result<Unit> = runCatching {
        firestore.collection("users").document(currentUserId)
            .update("following", FieldValue.arrayUnion(targetUserId)).await()
        firestore.collection("users").document(targetUserId)
            .update("followers", FieldValue.arrayUnion(currentUserId)).await()

        if (currentUserId.isNotEmpty() && targetUserId.isNotEmpty() && currentUserId != targetUserId) {
            runCatching {
                val senderName = if (currentUserName.isNotEmpty()) {
                    currentUserName
                } else {
                    val senderDoc = firestore.collection("users").document(currentUserId).get().await()
                    senderDoc.getString("displayName") ?: "Bir öğrenci"
                }

                notificationRepository.sendNotification(
                    userId = targetUserId,
                    senderId = currentUserId,
                    senderName = senderName,
                    senderPhotoUrl = currentUserPhotoUrl,
                    questionId = "",
                    type = "FOLLOW",
                    message = "$senderName seni takip etti 👤"
                )
            }
        }
    }

    suspend fun unfollowUser(currentUserId: String, targetUserId: String): Result<Unit> = runCatching {
        firestore.collection("users").document(currentUserId)
            .update("following", FieldValue.arrayRemove(targetUserId)).await()
        firestore.collection("users").document(targetUserId)
            .update("followers", FieldValue.arrayRemove(currentUserId)).await()
    }

    suspend fun updateVisibleBadges(uid: String, visibleBadges: List<String>): Result<Unit> = runCatching {
        firestore.collection("users").document(uid)
            .update("visibleBadges", visibleBadges).await()
    }

    suspend fun blockUser(currentUserId: String, targetUserId: String): Result<Unit> = runCatching {
        // Remove mutual follow relationships and add to blockedUsers
        firestore.collection("users").document(currentUserId)
            .update(
                "blockedUsers", FieldValue.arrayUnion(targetUserId),
                "following", FieldValue.arrayRemove(targetUserId),
                "followers", FieldValue.arrayRemove(targetUserId)
            ).await()
        firestore.collection("users").document(targetUserId)
            .update(
                "following", FieldValue.arrayRemove(currentUserId),
                "followers", FieldValue.arrayRemove(currentUserId)
            ).await()
    }

    suspend fun unblockUser(currentUserId: String, targetUserId: String): Result<Unit> = runCatching {
        firestore.collection("users").document(currentUserId)
            .update("blockedUsers", FieldValue.arrayRemove(targetUserId)).await()
    }

    suspend fun reportUser(
        targetUserId: String,
        reporterId: String,
        reason: String,
        note: String = ""
    ): Result<Unit> = runCatching {
        val reportId = java.util.UUID.randomUUID().toString()
        val reportData = mapOf(
            "id" to reportId,
            "targetId" to targetUserId,
            "reporterId" to reporterId,
            "type" to "USER",
            "reason" to reason,
            "note" to note,
            "createdAt" to java.util.Date()
        )
        firestore.collection("reports").document(reportId).set(reportData).await()
    }
}

