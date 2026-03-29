package com.gymtrack.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String = "",
    val dateOfBirth: Long?, // Stored as epoch millis
    val gender: String, // Gender enum name
    val height: Float? = null,
    val weight: Float? = null,
    val bodyFatPercentage: Float? = null,
    val experienceLevel: String, // ExperienceLevel enum name
    val weeklyWorkoutDays: Int = 0,
    val preferredWorkoutStyles: String, // Comma-separated list
    val primaryGoal: String, // FitnessGoal enum name
    val createdAt: Long = System.currentTimeMillis(),
    val profileImageUrl: String? = null,
    val isOnboardingCompleted: Boolean = false,
    // Settings
    val darkModeEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val autoStartTimer: Boolean = true,
    val restTimerDefault: Int = 90,
    val measurementUnit: String = "kg"
)

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val instructions: String,
    val muscleGroup: String, // MuscleGroup enum name
    val secondaryMuscles: String = "", // Comma-separated
    val equipment: String, // Equipment enum name
    val difficulty: String, // Difficulty enum name
    val isCustom: Boolean = false,
    val isFavorite: Boolean = false,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val calorieBurnRate: Float = 0f
)

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val scheduledDate: Long? = null,
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val durationMinutes: Int = 0,
    val totalVolume: Float = 0f,
    val totalSets: Int = 0,
    val totalReps: Int = 0,
    val status: String = "PLANNED",
    val notes: String = ""
)

@Entity(tableName = "workout_exercises")
data class WorkoutExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workoutId: Long,
    val exerciseId: Long,
    val restTimeSeconds: Int = 90,
    val notes: String = "",
    val orderIndex: Int = 0
)

@Entity(tableName = "exercise_sets")
data class ExerciseSetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workoutExerciseId: Long,
    val setNumber: Int,
    val weight: Float,
    val reps: Int,
    val isCompleted: Boolean = false,
    val isWarmup: Boolean = false,
    val isDropSet: Boolean = false,
    val isSuperset: Boolean = false,
    val rpe: Float? = null,
    val completedAt: Long? = null,
    val notes: String = ""
)

@Entity(tableName = "personal_records")
data class PersonalRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val exerciseId: Long,
    val weight: Float,
    val reps: Int,
    val oneRepMax: Float,
    val achievedAt: Long,
    val recordType: String
)

@Entity(tableName = "body_measurements")
data class BodyMeasurementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recordedAt: Long,
    val weight: Float? = null,
    val bodyFatPercentage: Float? = null,
    val muscleMass: Float? = null,
    val waterPercentage: Float? = null,
    val chest: Float? = null,
    val waist: Float? = null,
    val hips: Float? = null,
    val biceps: Float? = null,
    val thighs: Float? = null,
    val calves: Float? = null,
    val neck: Float? = null,
    val shoulders: Float? = null,
    val notes: String = ""
)

@Entity(tableName = "workout_templates")
data class WorkoutTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val estimatedDurationMinutes: Int = 0,
    val difficulty: String,
    val muscleGroups: String // Comma-separated
)

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val displayName: String,
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

@Entity(tableName = "community_posts")
data class CommunityPostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val authorId: Long,
    val authorName: String,
    val authorAvatarUrl: String? = null,
    val content: String,
    val imageUrl: String? = null,
    val postType: String = "GENERAL", // GENERAL, WORKOUT, ACHIEVEMENT, CHALLENGE
    val workoutId: Long? = null,
    val achievementId: Long? = null,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val iconName: String,
    val category: String, // WORKOUT, STREAK, SOCIAL, MILESTONE
    val requirement: Int, // Required value to unlock
    val xpReward: Int = 0,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val userId: Long
)

@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val challengeType: String, // WORKOUT_COUNT, STREAK, TOTAL_MINUTES
    val targetValue: Int,
    val currentValue: Int = 0,
    val startDate: Long,
    val endDate: Long,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val participantCount: Int = 1,
    val creatorId: Long,
    val xpReward: Int = 0
)

@Entity(tableName = "leaderboard_entries")
data class LeaderboardEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val userName: String,
    val avatarUrl: String? = null,
    val score: Int = 0,
    val rank: Int = 0,
    val category: String, // WEEKLY_WORKOUTS, TOTAL_STREAK, MONTHLY_MINUTES
    val period: String, // WEEKLY, MONTHLY, ALL_TIME
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "post_likes")
data class PostLikeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val postId: Long,
    val userId: Long,
    val createdAt: Long = System.currentTimeMillis()
)
