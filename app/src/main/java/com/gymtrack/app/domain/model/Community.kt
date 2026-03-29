package com.gymtrack.app.domain.model

import java.time.LocalDateTime

data class UserProfile(
    val id: Long = 0,
    val userId: Long,
    val displayName: String,
    val avatarUrl: String? = null,
    val bio: String = "",
    val location: String = "",
    val joinDate: LocalDateTime = LocalDateTime.now(),
    val totalWorkouts: Int = 0,
    val totalMinutes: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val level: Int = 1,
    val experiencePoints: Int = 0,
    val isPublic: Boolean = true
)

data class CommunityPost(
    val id: Long = 0,
    val authorId: Long,
    val authorName: String,
    val authorAvatarUrl: String? = null,
    val content: String,
    val imageUrl: String? = null,
    val postType: PostType = PostType.GENERAL,
    val workoutId: Long? = null,
    val achievementId: Long? = null,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val isLikedByCurrentUser: Boolean = false
)

enum class PostType {
    GENERAL,
    WORKOUT,
    ACHIEVEMENT,
    CHALLENGE
}

data class Achievement(
    val id: Long = 0,
    val name: String,
    val description: String,
    val iconName: String,
    val category: AchievementCategory,
    val requirement: Int,
    val xpReward: Int = 0,
    val isUnlocked: Boolean = false,
    val unlockedAt: LocalDateTime? = null,
    val userId: Long
)

enum class AchievementCategory {
    WORKOUT,
    STREAK,
    SOCIAL,
    MILESTONE
}

data class Challenge(
    val id: Long = 0,
    val title: String,
    val description: String,
    val challengeType: ChallengeType,
    val targetValue: Int,
    val currentValue: Int = 0,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val isCompleted: Boolean = false,
    val completedAt: LocalDateTime? = null,
    val participantCount: Int = 1,
    val creatorId: Long,
    val xpReward: Int = 0
)

enum class ChallengeType {
    WORKOUT_COUNT,
    STREAK,
    TOTAL_MINUTES
}

data class LeaderboardEntry(
    val id: Long = 0,
    val userId: Long,
    val userName: String,
    val avatarUrl: String? = null,
    val score: Int = 0,
    val rank: Int = 0,
    val category: LeaderboardCategory,
    val period: LeaderboardPeriod,
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class LeaderboardCategory {
    WEEKLY_WORKOUTS,
    TOTAL_STREAK,
    MONTHLY_MINUTES
}

enum class LeaderboardPeriod {
    WEEKLY,
    MONTHLY,
    ALL_TIME
}

data class PostLike(
    val id: Long = 0,
    val postId: Long,
    val userId: Long,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
