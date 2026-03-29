package com.gymtrack.app.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class FirebaseUserProfile(
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val bio: String = "",
    val location: String = "",
    val joinDate: Long = System.currentTimeMillis(),
    val totalWorkouts: Int = 0,
    val totalMinutes: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val level: Int = 1,
    val experiencePoints: Int = 0,
    val isPublic: Boolean = true
)

data class FirebasePost(
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String? = null,
    val content: String = "",
    val imageUrl: String? = null,
    val postType: String = "GENERAL",
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val likedBy: List<String> = emptyList()
)

data class FirebaseAchievement(
    val achievementId: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val iconName: String = "",
    val category: String = "",
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)

data class FirebaseChallenge(
    val challengeId: String = "",
    val title: String = "",
    val description: String = "",
    val challengeType: String = "",
    val targetValue: Int = 0,
    val startDate: Long = 0,
    val endDate: Long = 0,
    val creatorId: String = "",
    val participantIds: List<String> = emptyList(),
    val xpReward: Int = 0
)

data class FirebaseLeaderboardEntry(
    val userId: String = "",
    val userName: String = "",
    val avatarUrl: String? = null,
    val score: Int = 0,
    val category: String = "",
    val period: String = ""
)

@Singleton
class FirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val USERS_COLLECTION = "users"
        private const val POSTS_COLLECTION = "posts"
        private const val ACHIEVEMENTS_COLLECTION = "achievements"
        private const val CHALLENGES_COLLECTION = "challenges"
        private const val LEADERBOARD_COLLECTION = "leaderboard"
    }

    // User Profile Operations
    suspend fun createUserProfile(profile: FirebaseUserProfile): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(profile.userId)
                .set(profile, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(userId: String): Result<FirebaseUserProfile?> {
        return try {
            val doc = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            Result.success(doc.toObject(FirebaseUserProfile::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserProfileFlow(userId: String): Flow<FirebaseUserProfile?> = callbackFlow {
        val listener = firestore.collection(USERS_COLLECTION)
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(FirebaseUserProfile::class.java))
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateUserStats(userId: String, workouts: Int, minutes: Int, streak: Int): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .update(
                    mapOf(
                        "totalWorkouts" to workouts,
                        "totalMinutes" to minutes,
                        "currentStreak" to streak
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Post Operations
    suspend fun createPost(post: FirebasePost): Result<String> {
        return try {
            val docRef = firestore.collection(POSTS_COLLECTION).document()
            val postWithId = post.copy(postId = docRef.id)
            docRef.set(postWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllPosts(limit: Long = 50): Flow<List<FirebasePost>> = callbackFlow {
        val listener = firestore.collection(POSTS_COLLECTION)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val posts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FirebasePost::class.java)
                } ?: emptyList()
                trySend(posts)
            }
        awaitClose { listener.remove() }
    }

    fun getUserPosts(userId: String): Flow<List<FirebasePost>> = callbackFlow {
        val listener = firestore.collection(POSTS_COLLECTION)
            .whereEqualTo("authorId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val posts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FirebasePost::class.java)
                } ?: emptyList()
                trySend(posts)
            }
        awaitClose { listener.remove() }
    }

    suspend fun likePost(postId: String, userId: String): Result<Unit> {
        return try {
            val docRef = firestore.collection(POSTS_COLLECTION).document(postId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val likedBy = snapshot.get("likedBy") as? List<String> ?: emptyList()
                if (!likedBy.contains(userId)) {
                    transaction.update(docRef, "likedBy", likedBy + userId)
                    transaction.update(docRef, "likesCount", (snapshot.getLong("likesCount") ?: 0) + 1)
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unlikePost(postId: String, userId: String): Result<Unit> {
        return try {
            val docRef = firestore.collection(POSTS_COLLECTION).document(postId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val likedBy = snapshot.get("likedBy") as? List<String> ?: emptyList()
                if (likedBy.contains(userId)) {
                    transaction.update(docRef, "likedBy", likedBy - userId)
                    transaction.update(docRef, "likesCount", (snapshot.getLong("likesCount") ?: 0) - 1)
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Achievement Operations
    suspend fun unlockAchievement(achievement: FirebaseAchievement): Result<Unit> {
        return try {
            firestore.collection(ACHIEVEMENTS_COLLECTION)
                .document("${achievement.userId}_${achievement.achievementId}")
                .set(achievement.copy(isUnlocked = true, unlockedAt = System.currentTimeMillis()), SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserAchievements(userId: String): Flow<List<FirebaseAchievement>> = callbackFlow {
        val listener = firestore.collection(ACHIEVEMENTS_COLLECTION)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val achievements = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FirebaseAchievement::class.java)
                } ?: emptyList()
                trySend(achievements)
            }
        awaitClose { listener.remove() }
    }

    // Challenge Operations
    suspend fun createChallenge(challenge: FirebaseChallenge): Result<String> {
        return try {
            val docRef = firestore.collection(CHALLENGES_COLLECTION).document()
            val challengeWithId = challenge.copy(challengeId = docRef.id)
            docRef.set(challengeWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getActiveChallenges(): Flow<List<FirebaseChallenge>> = callbackFlow {
        val now = System.currentTimeMillis()
        val listener = firestore.collection(CHALLENGES_COLLECTION)
            .whereGreaterThan("endDate", now)
            .orderBy("endDate")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val challenges = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FirebaseChallenge::class.java)
                } ?: emptyList()
                trySend(challenges)
            }
        awaitClose { listener.remove() }
    }

    suspend fun joinChallenge(challengeId: String, userId: String): Result<Unit> {
        return try {
            val docRef = firestore.collection(CHALLENGES_COLLECTION).document(challengeId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val participants = snapshot.get("participantIds") as? List<String> ?: emptyList()
                if (!participants.contains(userId)) {
                    transaction.update(docRef, "participantIds", participants + userId)
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Leaderboard Operations
    suspend fun updateLeaderboardEntry(entry: FirebaseLeaderboardEntry): Result<Unit> {
        return try {
            val docId = "${entry.userId}_${entry.category}_${entry.period}"
            firestore.collection(LEADERBOARD_COLLECTION)
                .document(docId)
                .set(entry, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getLeaderboard(category: String, period: String, limit: Long = 50): Flow<List<FirebaseLeaderboardEntry>> = callbackFlow {
        val listener = firestore.collection(LEADERBOARD_COLLECTION)
            .whereEqualTo("category", category)
            .whereEqualTo("period", period)
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val entries = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FirebaseLeaderboardEntry::class.java)
                } ?: emptyList()
                trySend(entries)
            }
        awaitClose { listener.remove() }
    }
}
