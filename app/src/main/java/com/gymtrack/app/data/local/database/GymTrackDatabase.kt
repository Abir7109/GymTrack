package com.gymtrack.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gymtrack.app.data.local.dao.*
import com.gymtrack.app.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        ExerciseEntity::class,
        WorkoutEntity::class,
        WorkoutExerciseEntity::class,
        ExerciseSetEntity::class,
        PersonalRecordEntity::class,
        BodyMeasurementEntity::class,
        WorkoutTemplateEntity::class,
        UserProfileEntity::class,
        CommunityPostEntity::class,
        AchievementEntity::class,
        ChallengeEntity::class,
        LeaderboardEntryEntity::class,
        PostLikeEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class GymTrackDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutExerciseDao(): WorkoutExerciseDao
    abstract fun exerciseSetDao(): ExerciseSetDao
    abstract fun personalRecordDao(): PersonalRecordDao
    abstract fun bodyMeasurementDao(): BodyMeasurementDao
    abstract fun workoutTemplateDao(): WorkoutTemplateDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun communityPostDao(): CommunityPostDao
    abstract fun achievementDao(): AchievementDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun leaderboardDao(): LeaderboardDao
    abstract fun postLikeDao(): PostLikeDao
}
