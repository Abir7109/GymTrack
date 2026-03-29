package com.gymtrack.app.domain.repository

import com.gymtrack.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getCurrentUser(): Flow<User?>
    suspend fun getCurrentUserOnce(): User?
    suspend fun saveUser(user: User): Long
    suspend fun updateUser(user: User)
    suspend fun markOnboardingCompleted(userId: Long)
    suspend fun deleteUser()
}

interface ExerciseRepository {
    fun getAllExercises(): Flow<List<Exercise>>
    fun getExercisesByMuscleGroup(muscleGroup: MuscleGroup): Flow<List<Exercise>>
    fun getFavoriteExercises(): Flow<List<Exercise>>
    fun getCustomExercises(): Flow<List<Exercise>>
    fun searchExercises(query: String): Flow<List<Exercise>>
    suspend fun getExerciseById(id: Long): Exercise?
    suspend fun insertExercise(exercise: Exercise): Long
    suspend fun updateExercise(exercise: Exercise)
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)
    suspend fun deleteExercise(exercise: Exercise)
    suspend fun seedDefaultExercises()
}

interface WorkoutRepository {
    fun getAllWorkouts(): Flow<List<Workout>>
    fun getCompletedWorkouts(): Flow<List<Workout>>
    fun getActiveWorkout(): Flow<Workout?>
    suspend fun getActiveWorkoutOnce(): Workout?
    fun getWorkoutsInDateRange(startDate: Long, endDate: Long): Flow<List<Workout>>
    suspend fun getWorkoutById(id: Long): Workout?
    suspend fun getCompletedWorkoutCountSince(since: Long): Int
    suspend fun getTotalWorkoutMinutesSince(since: Long): Int
    suspend fun getTotalCompletedWorkoutCount(): Int
    suspend fun getCompletedWorkoutDatesSince(since: Long): List<Long>
    suspend fun startWorkout(name: String): Long
    suspend fun updateWorkout(workout: Workout)
    suspend fun completeWorkout(workoutId: Long, durationMinutes: Int, totalVolume: Float, totalSets: Int, totalReps: Int)
    suspend fun deleteWorkout(workout: Workout)
}

interface WorkoutExerciseRepository {
    fun getExercisesForWorkout(workoutId: Long): Flow<List<WorkoutExercise>>
    suspend fun addExerciseToWorkout(workoutId: Long, exerciseId: Long, restTimeSeconds: Int = 90): Long
    suspend fun updateWorkoutExercise(workoutExercise: WorkoutExercise)
    suspend fun removeExerciseFromWorkout(workoutExerciseId: Long)
    suspend fun reorderExercises(workoutId: Long, exerciseIds: List<Long>)
}

interface ExerciseSetRepository {
    fun getSetsForWorkoutExercise(workoutExerciseId: Long): Flow<List<ExerciseSet>>
    suspend fun addSet(workoutExerciseId: Long, weight: Float, reps: Int, isWarmup: Boolean = false): Long
    suspend fun updateSet(set: ExerciseSet)
    suspend fun completeSet(setId: Long, isCompleted: Boolean)
    suspend fun deleteSet(set: ExerciseSet)
}

interface PersonalRecordRepository {
    fun getAllRecords(): Flow<List<PersonalRecord>>
    fun getRecordsForExercise(exerciseId: Long): Flow<List<PersonalRecord>>
    suspend fun getHeaviestWeightForExercise(exerciseId: Long): PersonalRecord?
    suspend fun checkAndSaveRecord(exerciseId: Long, weight: Float, reps: Int): Boolean
}

interface BodyMeasurementRepository {
    fun getAllMeasurements(): Flow<List<BodyMeasurement>>
    fun getMeasurementsInRange(startDate: Long, endDate: Long): Flow<List<BodyMeasurement>>
    fun getLatestMeasurement(): Flow<BodyMeasurement?>
    suspend fun getLatestMeasurementOnce(): BodyMeasurement?
    suspend fun addMeasurement(measurement: BodyMeasurement): Long
    suspend fun updateMeasurement(measurement: BodyMeasurement)
    suspend fun deleteMeasurement(measurement: BodyMeasurement)
}

interface WorkoutTemplateRepository {
    fun getAllTemplates(): Flow<List<WorkoutTemplate>>
    fun getTemplatesByMuscleGroup(muscleGroup: MuscleGroup): Flow<List<WorkoutTemplate>>
    suspend fun getTemplateById(id: Long): WorkoutTemplate?
    suspend fun createTemplate(template: WorkoutTemplate): Long
    suspend fun updateTemplate(template: WorkoutTemplate)
    suspend fun deleteTemplate(template: WorkoutTemplate)
    suspend fun seedDefaultTemplates()
}

interface CommunityRepository {
    // User Profile
    fun getUserProfile(userId: Long): Flow<UserProfile?>
    suspend fun getUserProfileOnce(userId: Long): UserProfile?
    suspend fun createOrUpdateProfile(profile: UserProfile): Long
    suspend fun updateUserStats(userId: Long, workouts: Int, minutes: Int, streak: Int)
    suspend fun addExperience(userId: Long, xp: Int)
    
    // Posts
    fun getAllPosts(): Flow<List<CommunityPost>>
    fun getUserPosts(userId: Long): Flow<List<CommunityPost>>
    suspend fun createPost(post: CommunityPost): Long
    suspend fun deletePost(post: CommunityPost)
    suspend fun likePost(postId: Long, userId: Long)
    suspend fun unlikePost(postId: Long, userId: Long)
    
    // Achievements
    fun getUserAchievements(userId: Long): Flow<List<Achievement>>
    fun getUnlockedAchievements(userId: Long): Flow<List<Achievement>>
    suspend fun seedDefaultAchievements(userId: Long)
    suspend fun checkAndUnlockAchievements(userId: Long)
    
    // Challenges
    fun getActiveChallenges(): Flow<List<Challenge>>
    fun getUserChallenges(userId: Long): Flow<List<Challenge>>
    suspend fun createChallenge(challenge: Challenge): Long
    suspend fun updateChallengeProgress(challengeId: Long, value: Int)
    
    // Leaderboard
    fun getLeaderboard(category: String, period: String): Flow<List<LeaderboardEntry>>
    suspend fun updateLeaderboard(userId: Long, userName: String, category: String, period: String, score: Int)
}
