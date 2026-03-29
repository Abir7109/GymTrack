package com.gymtrack.app.data.local.dao

import androidx.room.*
import com.gymtrack.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUser(): Flow<UserEntity?>
    
    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUserOnce(): UserEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long
    
    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Query("UPDATE users SET isOnboardingCompleted = 1 WHERE id = :userId")
    suspend fun markOnboardingCompleted(userId: Long)
    
    @Delete
    suspend fun deleteUser(user: UserEntity)
    
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>
    
    @Query("SELECT * FROM exercises WHERE muscleGroup = :muscleGroup ORDER BY name ASC")
    fun getExercisesByMuscleGroup(muscleGroup: String): Flow<List<ExerciseEntity>>
    
    @Query("SELECT * FROM exercises WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteExercises(): Flow<List<ExerciseEntity>>
    
    @Query("SELECT * FROM exercises WHERE isCustom = 1 ORDER BY name ASC")
    fun getCustomExercises(): Flow<List<ExerciseEntity>>
    
    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchExercises(query: String): Flow<List<ExerciseEntity>>
    
    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: Long): ExerciseEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)
    
    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)
    
    @Query("UPDATE exercises SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)
    
    @Delete
    suspend fun deleteExercise(exercise: ExerciseEntity)
    
    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getExerciseCount(): Int
}

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY startedAt DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>
    
    @Query("SELECT * FROM workouts WHERE status = 'COMPLETED' ORDER BY completedAt DESC")
    fun getCompletedWorkouts(): Flow<List<WorkoutEntity>>
    
    @Query("SELECT * FROM workouts WHERE status = 'IN_PROGRESS' LIMIT 1")
    fun getActiveWorkout(): Flow<WorkoutEntity?>
    
    @Query("SELECT * FROM workouts WHERE status = 'IN_PROGRESS' LIMIT 1")
    suspend fun getActiveWorkoutOnce(): WorkoutEntity?
    
    @Query("SELECT * FROM workouts WHERE startedAt >= :startDate AND startedAt <= :endDate ORDER BY startedAt DESC")
    fun getWorkoutsInDateRange(startDate: Long, endDate: Long): Flow<List<WorkoutEntity>>
    
    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getWorkoutById(id: Long): WorkoutEntity?
    
    @Query("SELECT COUNT(*) FROM workouts WHERE status = 'COMPLETED' AND completedAt >= :since")
    suspend fun getCompletedWorkoutCountSince(since: Long): Int
    
    @Query("SELECT SUM(durationMinutes) FROM workouts WHERE status = 'COMPLETED' AND completedAt >= :since")
    suspend fun getTotalWorkoutMinutesSince(since: Long): Int?
    
    @Query("SELECT COUNT(*) FROM workouts WHERE status = 'COMPLETED'")
    suspend fun getTotalCompletedWorkoutCount(): Int

    @Query("SELECT completedAt FROM workouts WHERE status = 'COMPLETED' AND completedAt >= :since ORDER BY completedAt DESC")
    suspend fun getCompletedWorkoutDatesSince(since: Long): List<Long>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long
    
    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)
    
    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)
}

@Dao
interface WorkoutExerciseDao {
    @Query("SELECT * FROM workout_exercises WHERE workoutId = :workoutId ORDER BY orderIndex ASC")
    fun getExercisesForWorkout(workoutId: Long): Flow<List<WorkoutExerciseEntity>>
    
    @Query("SELECT * FROM workout_exercises WHERE id = :id")
    suspend fun getWorkoutExerciseById(id: Long): WorkoutExerciseEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExercise(workoutExercise: WorkoutExerciseEntity): Long
    
    @Update
    suspend fun updateWorkoutExercise(workoutExercise: WorkoutExerciseEntity)
    
    @Delete
    suspend fun deleteWorkoutExercise(workoutExercise: WorkoutExerciseEntity)
    
    @Query("DELETE FROM workout_exercises WHERE workoutId = :workoutId")
    suspend fun deleteAllForWorkout(workoutId: Long)
}

@Dao
interface ExerciseSetDao {
    @Query("SELECT * FROM exercise_sets WHERE workoutExerciseId = :workoutExerciseId ORDER BY setNumber ASC")
    fun getSetsForWorkoutExercise(workoutExerciseId: Long): Flow<List<ExerciseSetEntity>>
    
    @Query("SELECT * FROM exercise_sets WHERE id = :id")
    suspend fun getSetById(id: Long): ExerciseSetEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: ExerciseSetEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<ExerciseSetEntity>)
    
    @Update
    suspend fun updateSet(set: ExerciseSetEntity)
    
    @Query("UPDATE exercise_sets SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :id")
    suspend fun markSetCompleted(id: Long, isCompleted: Boolean, completedAt: Long?)
    
    @Delete
    suspend fun deleteSet(set: ExerciseSetEntity)
    
    @Query("DELETE FROM exercise_sets WHERE workoutExerciseId = :workoutExerciseId")
    suspend fun deleteAllForWorkoutExercise(workoutExerciseId: Long)
}

@Dao
interface PersonalRecordDao {
    @Query("SELECT * FROM personal_records ORDER BY achievedAt DESC")
    fun getAllPersonalRecords(): Flow<List<PersonalRecordEntity>>
    
    @Query("SELECT * FROM personal_records WHERE exerciseId = :exerciseId ORDER BY achievedAt DESC")
    fun getRecordsForExercise(exerciseId: Long): Flow<List<PersonalRecordEntity>>
    
    @Query("SELECT * FROM personal_records WHERE recordType = :recordType ORDER BY achievedAt DESC")
    fun getRecordsByType(recordType: String): Flow<List<PersonalRecordEntity>>
    
    @Query("SELECT * FROM personal_records WHERE exerciseId = :exerciseId AND recordType = 'HEAVIEST_WEIGHT' ORDER BY weight DESC LIMIT 1")
    suspend fun getHeaviestWeightForExercise(exerciseId: Long): PersonalRecordEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: PersonalRecordEntity): Long
    
    @Delete
    suspend fun deleteRecord(record: PersonalRecordEntity)
}

@Dao
interface BodyMeasurementDao {
    @Query("SELECT * FROM body_measurements ORDER BY recordedAt DESC")
    fun getAllMeasurements(): Flow<List<BodyMeasurementEntity>>
    
    @Query("SELECT * FROM body_measurements WHERE recordedAt >= :startDate AND recordedAt <= :endDate ORDER BY recordedAt DESC")
    fun getMeasurementsInRange(startDate: Long, endDate: Long): Flow<List<BodyMeasurementEntity>>
    
    @Query("SELECT * FROM body_measurements ORDER BY recordedAt DESC LIMIT 1")
    fun getLatestMeasurement(): Flow<BodyMeasurementEntity?>
    
    @Query("SELECT * FROM body_measurements ORDER BY recordedAt DESC LIMIT 1")
    suspend fun getLatestMeasurementOnce(): BodyMeasurementEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: BodyMeasurementEntity): Long
    
    @Update
    suspend fun updateMeasurement(measurement: BodyMeasurementEntity)
    
    @Delete
    suspend fun deleteMeasurement(measurement: BodyMeasurementEntity)
}

@Dao
interface WorkoutTemplateDao {
    @Query("SELECT * FROM workout_templates ORDER BY name ASC")
    fun getAllTemplates(): Flow<List<WorkoutTemplateEntity>>
    
    @Query("SELECT * FROM workout_templates WHERE muscleGroups LIKE '%' || :muscleGroup || '%'")
    fun getTemplatesByMuscleGroup(muscleGroup: String): Flow<List<WorkoutTemplateEntity>>
    
    @Query("SELECT * FROM workout_templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): WorkoutTemplateEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: WorkoutTemplateEntity): Long
    
    @Update
    suspend fun updateTemplate(template: WorkoutTemplateEntity)
    
    @Delete
    suspend fun deleteTemplate(template: WorkoutTemplateEntity)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    fun getUserProfile(userId: Long): Flow<UserProfileEntity?>
    
    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    suspend fun getUserProfileOnce(userId: Long): UserProfileEntity?
    
    @Query("SELECT * FROM user_profiles WHERE isPublic = 1 ORDER BY experiencePoints DESC")
    fun getAllPublicProfiles(): Flow<List<UserProfileEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity): Long
    
    @Query("UPDATE user_profiles SET totalWorkouts = :workouts, totalMinutes = :minutes, currentStreak = :streak WHERE userId = :userId")
    suspend fun updateStats(userId: Long, workouts: Int, minutes: Int, streak: Int)
    
    @Query("UPDATE user_profiles SET experiencePoints = experiencePoints + :xp, level = :level WHERE userId = :userId")
    suspend fun addExperience(userId: Long, xp: Int, level: Int)
}

@Dao
interface CommunityPostDao {
    @Query("SELECT * FROM community_posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<CommunityPostEntity>>
    
    @Query("SELECT * FROM community_posts WHERE authorId = :userId ORDER BY createdAt DESC")
    fun getUserPosts(userId: Long): Flow<List<CommunityPostEntity>>
    
    @Query("SELECT * FROM community_posts WHERE postType = :type ORDER BY createdAt DESC")
    fun getPostsByType(type: String): Flow<List<CommunityPostEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CommunityPostEntity): Long
    
    @Delete
    suspend fun deletePost(post: CommunityPostEntity)
    
    @Query("UPDATE community_posts SET likesCount = likesCount + 1 WHERE id = :postId")
    suspend fun incrementLikes(postId: Long)
    
    @Query("UPDATE community_posts SET likesCount = likesCount - 1 WHERE id = :postId AND likesCount > 0")
    suspend fun decrementLikes(postId: Long)
    
    @Query("UPDATE community_posts SET commentsCount = commentsCount + 1 WHERE id = :postId")
    suspend fun incrementComments(postId: Long)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements WHERE userId = :userId ORDER BY category, name")
    fun getUserAchievements(userId: Long): Flow<List<AchievementEntity>>
    
    @Query("SELECT * FROM achievements WHERE userId = :userId AND isUnlocked = 1 ORDER BY unlockedAt DESC")
    fun getUnlockedAchievements(userId: Long): Flow<List<AchievementEntity>>
    
    @Query("SELECT * FROM achievements WHERE userId = :userId AND isUnlocked = 0")
    fun getLockedAchievements(userId: Long): Flow<List<AchievementEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)
    
    @Query("UPDATE achievements SET isUnlocked = 1, unlockedAt = :unlockedAt WHERE id = :achievementId")
    suspend fun unlockAchievement(achievementId: Long, unlockedAt: Long = System.currentTimeMillis())
    
    @Query("SELECT COUNT(*) FROM achievements WHERE userId = :userId AND isUnlocked = 1")
    suspend fun getUnlockedCount(userId: Long): Int
}

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenges WHERE endDate > :now ORDER BY endDate ASC")
    fun getActiveChallenges(now: Long = System.currentTimeMillis()): Flow<List<ChallengeEntity>>
    
    @Query("SELECT * FROM challenges WHERE creatorId = :userId ORDER BY startDate DESC")
    fun getUserChallenges(userId: Long): Flow<List<ChallengeEntity>>
    
    @Query("SELECT * FROM challenges WHERE isCompleted = 1 AND creatorId = :userId ORDER BY completedAt DESC")
    fun getCompletedChallenges(userId: Long): Flow<List<ChallengeEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: ChallengeEntity): Long
    
    @Update
    suspend fun updateChallenge(challenge: ChallengeEntity)
    
    @Query("UPDATE challenges SET currentValue = :value WHERE id = :challengeId")
    suspend fun updateProgress(challengeId: Long, value: Int)
    
    @Query("UPDATE challenges SET isCompleted = 1, completedAt = :completedAt WHERE id = :challengeId")
    suspend fun completeChallenge(challengeId: Long, completedAt: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM challenges WHERE endDate < :now AND isCompleted = 0")
    suspend fun deleteExpiredChallenges(now: Long = System.currentTimeMillis())
}

@Dao
interface LeaderboardDao {
    @Query("SELECT * FROM leaderboard_entries WHERE category = :category AND period = :period ORDER BY score DESC LIMIT :limit")
    fun getLeaderboard(category: String, period: String, limit: Int = 50): Flow<List<LeaderboardEntryEntity>>
    
    @Query("SELECT * FROM leaderboard_entries WHERE userId = :userId AND category = :category AND period = :period LIMIT 1")
    suspend fun getUserRank(userId: Long, category: String, period: String): LeaderboardEntryEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateEntry(entry: LeaderboardEntryEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<LeaderboardEntryEntity>)
    
    @Query("DELETE FROM leaderboard_entries WHERE category = :category AND period = :period")
    suspend fun clearLeaderboard(category: String, period: String)
}

@Dao
interface PostLikeDao {
    @Query("SELECT * FROM post_likes WHERE postId = :postId AND userId = :userId LIMIT 1")
    suspend fun getLike(postId: Long, userId: Long): PostLikeEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(like: PostLikeEntity): Long
    
    @Query("DELETE FROM post_likes WHERE postId = :postId AND userId = :userId")
    suspend fun deleteLike(postId: Long, userId: Long)
    
    @Query("SELECT COUNT(*) FROM post_likes WHERE postId = :postId")
    suspend fun getLikeCount(postId: Long): Int
}
