package com.gymtrack.app.data.repository

import com.gymtrack.app.data.local.dao.*
import com.gymtrack.app.data.local.entity.*
import com.gymtrack.app.data.mapper.toDomain
import com.gymtrack.app.data.mapper.toEntity
import com.gymtrack.app.domain.model.*
import com.gymtrack.app.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {
    override fun getCurrentUser(): Flow<User?> = userDao.getCurrentUser().map { entity -> entity?.toDomain() }
    override suspend fun getCurrentUserOnce(): User? = userDao.getCurrentUserOnce()?.toDomain()
    override suspend fun saveUser(user: User): Long = userDao.insertUser(user.toEntity())
    override suspend fun updateUser(user: User) = userDao.updateUser(user.toEntity())
    override suspend fun markOnboardingCompleted(userId: Long) = userDao.markOnboardingCompleted(userId)
    override suspend fun deleteUser() = userDao.deleteAllUsers()
}

@Singleton
class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao
) : ExerciseRepository {
    override fun getAllExercises(): Flow<List<Exercise>> = 
        exerciseDao.getAllExercises().map { list -> list.map { it.toDomain() } }
    
    override fun getExercisesByMuscleGroup(muscleGroup: MuscleGroup): Flow<List<Exercise>> =
        exerciseDao.getExercisesByMuscleGroup(muscleGroup.name).map { list -> list.map { it.toDomain() } }
    
    override fun getFavoriteExercises(): Flow<List<Exercise>> =
        exerciseDao.getFavoriteExercises().map { list -> list.map { it.toDomain() } }
    
    override fun getCustomExercises(): Flow<List<Exercise>> =
        exerciseDao.getCustomExercises().map { list -> list.map { it.toDomain() } }
    
    override fun searchExercises(query: String): Flow<List<Exercise>> =
        exerciseDao.searchExercises(query).map { list -> list.map { it.toDomain() } }
    
    override suspend fun getExerciseById(id: Long): Exercise? = exerciseDao.getExerciseById(id)?.toDomain()
    
    override suspend fun insertExercise(exercise: Exercise): Long = exerciseDao.insertExercise(exercise.toEntity())
    
    override suspend fun updateExercise(exercise: Exercise) = exerciseDao.updateExercise(exercise.toEntity())
    
    override suspend fun toggleFavorite(id: Long, isFavorite: Boolean) = 
        exerciseDao.updateFavoriteStatus(id, isFavorite)
    
    override suspend fun deleteExercise(exercise: Exercise) = exerciseDao.deleteExercise(exercise.toEntity())
    
    override suspend fun seedDefaultExercises() {
        val count = exerciseDao.getExerciseCount()
        if (count == 0) {
            val defaultExercises = getDefaultExercises()
            exerciseDao.insertExercises(defaultExercises.map { it.toEntity() })
        }
    }
    
    private fun getDefaultExercises(): List<Exercise> = listOf(
        // Chest (MET values from standard exercise data)
        Exercise(name = "Barbell Bench Press", description = "Classic chest compound", instructions = "Lie flat on bench, grip bar slightly wider than shoulder width, lower to chest, press up explosively.", muscleGroup = MuscleGroup.CHEST, equipment = Equipment.BARBELL, difficulty = Difficulty.INTERMEDIATE, calorieBurnRate = 6.0f),
        Exercise(name = "Incline Dumbbell Press", description = "Upper chest builder", instructions = "Set bench to 30-45 degrees, press dumbbells from chest level overhead.", muscleGroup = MuscleGroup.CHEST, equipment = Equipment.DUMBBELL, difficulty = Difficulty.INTERMEDIATE, calorieBurnRate = 5.0f),
        Exercise(name = "Cable Fly", description = "Chest isolation exercise", instructions = "Stand between cables, bring handles together in arc motion keeping slight bend in elbows.", muscleGroup = MuscleGroup.CHEST, equipment = Equipment.CABLE, difficulty = Difficulty.BEGINNER, calorieBurnRate = 3.5f),
        Exercise(name = "Push-ups", description = "Bodyweight chest exercise", instructions = "Hands shoulder-width apart, lower chest to floor, push back up. Keep core tight.", muscleGroup = MuscleGroup.CHEST, equipment = Equipment.BODYWEIGHT, difficulty = Difficulty.BEGINNER, calorieBurnRate = 8.0f),

        // Back
        Exercise(name = "Pull-ups", description = "Back compound movement", instructions = "Grip bar overhead, pull chin above bar, lower with control.", muscleGroup = MuscleGroup.BACK, equipment = Equipment.BODYWEIGHT, difficulty = Difficulty.INTERMEDIATE, calorieBurnRate = 8.0f),
        Exercise(name = "Barbell Row", description = "Back thickness builder", instructions = "Hinge at hips, pull bar to lower chest, squeeze shoulder blades.", muscleGroup = MuscleGroup.BACK, equipment = Equipment.BARBELL, difficulty = Difficulty.INTERMEDIATE, calorieBurnRate = 6.0f),
        Exercise(name = "Lat Pulldown", description = "Machine back exercise", instructions = "Grip wide, pull bar to upper chest, control the return.", muscleGroup = MuscleGroup.BACK, equipment = Equipment.CABLE, difficulty = Difficulty.BEGINNER, calorieBurnRate = 5.0f),
        Exercise(name = "Deadlift", description = "Full posterior chain compound", instructions = "Feet hip-width, grip bar, drive through heels, lock out hips at top.", muscleGroup = MuscleGroup.BACK, equipment = Equipment.BARBELL, difficulty = Difficulty.ADVANCED, calorieBurnRate = 6.0f),

        // Shoulders
        Exercise(name = "Overhead Press", description = "Shoulder compound builder", instructions = "Press barbell from shoulders overhead, lock out arms, control descent.", muscleGroup = MuscleGroup.SHOULDERS, equipment = Equipment.BARBELL, difficulty = Difficulty.INTERMEDIATE, calorieBurnRate = 6.0f),
        Exercise(name = "Lateral Raise", description = "Side delt isolation", instructions = "Raise dumbbells to sides until parallel with floor, slight bend in elbows.", muscleGroup = MuscleGroup.SHOULDERS, equipment = Equipment.DUMBBELL, difficulty = Difficulty.BEGINNER, calorieBurnRate = 4.0f),

        // Biceps
        Exercise(name = "Barbell Curl", description = "Bicep mass builder", instructions = "Curl barbell from thighs to shoulders, keep elbows pinned to sides.", muscleGroup = MuscleGroup.BICEPS, equipment = Equipment.BARBELL, difficulty = Difficulty.BEGINNER, calorieBurnRate = 5.0f),
        Exercise(name = "Dumbbell Curl", description = "Bicep isolation", instructions = "Alternate curling dumbbells, supinate wrist at top for peak contraction.", muscleGroup = MuscleGroup.BICEPS, equipment = Equipment.DUMBBELL, difficulty = Difficulty.BEGINNER, calorieBurnRate = 5.0f),

        // Triceps
        Exercise(name = "Tricep Pushdown", description = "Tricep isolation", instructions = "Push cable handle down until arms are straight, keep elbows at sides.", muscleGroup = MuscleGroup.TRICEPS, equipment = Equipment.CABLE, difficulty = Difficulty.BEGINNER, calorieBurnRate = 4.0f),
        Exercise(name = "Dips", description = "Bodyweight tricep builder", instructions = "Lower body between parallel bars until elbows at 90 degrees, press back up.", muscleGroup = MuscleGroup.TRICEPS, equipment = Equipment.BODYWEIGHT, difficulty = Difficulty.INTERMEDIATE, calorieBurnRate = 8.0f),

        // Quadriceps
        Exercise(name = "Barbell Squat", description = "King of leg exercises", instructions = "Bar on upper back, squat until thighs parallel, drive through heels.", muscleGroup = MuscleGroup.QUADRICEPS, equipment = Equipment.BARBELL, difficulty = Difficulty.INTERMEDIATE, calorieBurnRate = 8.0f),
        Exercise(name = "Leg Press", description = "Machine leg builder", instructions = "Feet shoulder-width on platform, press weight, don't lock knees.", muscleGroup = MuscleGroup.QUADRICEPS, equipment = Equipment.LEG_PRESS, difficulty = Difficulty.BEGINNER, calorieBurnRate = 6.0f),
        Exercise(name = "Lunges", description = "Unilateral leg exercise", instructions = "Step forward, lower back knee to floor, push back to standing.", muscleGroup = MuscleGroup.QUADRICEPS, equipment = Equipment.DUMBBELL, difficulty = Difficulty.BEGINNER, calorieBurnRate = 8.0f),

        // Hamstrings
        Exercise(name = "Romanian Deadlift", description = "Hamstring focused hinge", instructions = "Hold barbell, hinge at hips keeping back flat, feel stretch in hamstrings.", muscleGroup = MuscleGroup.HAMSTRINGS, equipment = Equipment.BARBELL, difficulty = Difficulty.INTERMEDIATE, calorieBurnRate = 6.0f),
        Exercise(name = "Leg Curl", description = "Hamstring isolation", instructions = "Curl weight toward glutes on machine, control the negative.", muscleGroup = MuscleGroup.HAMSTRINGS, equipment = Equipment.MACHINE, difficulty = Difficulty.BEGINNER, calorieBurnRate = 4.0f),

        // Glutes
        Exercise(name = "Hip Thrust", description = "Glute activation compound", instructions = "Upper back on bench, drive hips up with barbell, squeeze glutes at top.", muscleGroup = MuscleGroup.GLUTES, equipment = Equipment.BARBELL, difficulty = Difficulty.INTERMEDIATE, calorieBurnRate = 6.0f),

        // Core
        Exercise(name = "Plank", description = "Core stability hold", instructions = "Hold body straight on forearms, engage core, don't let hips sag.", muscleGroup = MuscleGroup.CORE, equipment = Equipment.BODYWEIGHT, difficulty = Difficulty.BEGINNER, calorieBurnRate = 3.0f),
        Exercise(name = "Crunches", description = "Ab isolation movement", instructions = "Lying on back, curl shoulders toward hips, don't pull on neck.", muscleGroup = MuscleGroup.CORE, equipment = Equipment.BODYWEIGHT, difficulty = Difficulty.BEGINNER, calorieBurnRate = 3.8f)
    )
}

@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutDao
) : WorkoutRepository {
    override fun getAllWorkouts(): Flow<List<Workout>> = 
        workoutDao.getAllWorkouts().map { list -> list.map { it.toDomain() } }
    
    override fun getCompletedWorkouts(): Flow<List<Workout>> =
        workoutDao.getCompletedWorkouts().map { list -> list.map { it.toDomain() } }
    
    override fun getActiveWorkout(): Flow<Workout?> = 
        workoutDao.getActiveWorkout().map { entity -> entity?.toDomain() }
    
    override suspend fun getActiveWorkoutOnce(): Workout? = 
        workoutDao.getActiveWorkoutOnce()?.toDomain()
    
    override fun getWorkoutsInDateRange(startDate: Long, endDate: Long): Flow<List<Workout>> =
        workoutDao.getWorkoutsInDateRange(startDate, endDate).map { list -> list.map { it.toDomain() } }
    
    override suspend fun getWorkoutById(id: Long): Workout? = workoutDao.getWorkoutById(id)?.toDomain()
    
    override suspend fun getCompletedWorkoutCountSince(since: Long): Int = 
        workoutDao.getCompletedWorkoutCountSince(since)
    
    override suspend fun getTotalWorkoutMinutesSince(since: Long): Int = 
        workoutDao.getTotalWorkoutMinutesSince(since) ?: 0
    
    override suspend fun getTotalCompletedWorkoutCount(): Int =
        workoutDao.getTotalCompletedWorkoutCount()

    override suspend fun getCompletedWorkoutDatesSince(since: Long): List<Long> =
        workoutDao.getCompletedWorkoutDatesSince(since)
    
    override suspend fun startWorkout(name: String): Long {
        val workout = WorkoutEntity(
            name = name,
            startedAt = System.currentTimeMillis(),
            status = "IN_PROGRESS"
        )
        return workoutDao.insertWorkout(workout)
    }
    
    override suspend fun updateWorkout(workout: Workout) = workoutDao.updateWorkout(workout.toEntity())
    
    override suspend fun completeWorkout(workoutId: Long, durationMinutes: Int, totalVolume: Float, totalSets: Int, totalReps: Int) {
        val existing = workoutDao.getWorkoutById(workoutId)
        existing?.let { workout ->
            workoutDao.updateWorkout(
                workout.copy(
                    status = "COMPLETED",
                    completedAt = System.currentTimeMillis(),
                    durationMinutes = durationMinutes,
                    totalVolume = totalVolume,
                    totalSets = totalSets,
                    totalReps = totalReps
                )
            )
        }
    }
    
    override suspend fun deleteWorkout(workout: Workout) = workoutDao.deleteWorkout(workout.toEntity())
}

@Singleton
class BodyMeasurementRepositoryImpl @Inject constructor(
    private val bodyMeasurementDao: BodyMeasurementDao
) : BodyMeasurementRepository {
    override fun getAllMeasurements(): Flow<List<BodyMeasurement>> =
        bodyMeasurementDao.getAllMeasurements().map { list -> list.map { it.toDomain() } }
    
    override fun getMeasurementsInRange(startDate: Long, endDate: Long): Flow<List<BodyMeasurement>> =
        bodyMeasurementDao.getMeasurementsInRange(startDate, endDate).map { list -> list.map { it.toDomain() } }
    
    override fun getLatestMeasurement(): Flow<BodyMeasurement?> =
        bodyMeasurementDao.getLatestMeasurement().map { entity -> entity?.toDomain() }
    
    override suspend fun getLatestMeasurementOnce(): BodyMeasurement? =
        bodyMeasurementDao.getLatestMeasurementOnce()?.toDomain()
    
    override suspend fun addMeasurement(measurement: BodyMeasurement): Long =
        bodyMeasurementDao.insertMeasurement(measurement.toEntity())
    
    override suspend fun updateMeasurement(measurement: BodyMeasurement) =
        bodyMeasurementDao.updateMeasurement(measurement.toEntity())
    
    override suspend fun deleteMeasurement(measurement: BodyMeasurement) =
        bodyMeasurementDao.deleteMeasurement(measurement.toEntity())
}

@Singleton
class PersonalRecordRepositoryImpl @Inject constructor(
    private val personalRecordDao: PersonalRecordDao
) : PersonalRecordRepository {
    override fun getAllRecords(): Flow<List<PersonalRecord>> =
        personalRecordDao.getAllPersonalRecords().map { list: List<PersonalRecordEntity> -> list.map { entity: PersonalRecordEntity -> entity.toDomain() } }

    override fun getRecordsForExercise(exerciseId: Long): Flow<List<PersonalRecord>> =
        personalRecordDao.getRecordsForExercise(exerciseId).map { list: List<PersonalRecordEntity> -> list.map { entity: PersonalRecordEntity -> entity.toDomain() } }

    override suspend fun getHeaviestWeightForExercise(exerciseId: Long): PersonalRecord? =
        personalRecordDao.getHeaviestWeightForExercise(exerciseId)?.toDomain()

    override suspend fun checkAndSaveRecord(exerciseId: Long, weight: Float, reps: Int): Boolean {
        val currentRecord = getHeaviestWeightForExercise(exerciseId)
        val newOneRepMax = weight * (1 + reps / 30f) // Epley formula
        val currentOneRepMax = currentRecord?.let { it.weight * (1 + it.reps / 30f) } ?: 0f

        return if (newOneRepMax > currentOneRepMax) {
            val record = PersonalRecord(
                id = 0,
                exercise = Exercise(
                    id = exerciseId,
                    name = "Unknown",
                    description = "",
                    instructions = "",
                    muscleGroup = MuscleGroup.CHEST,
                    equipment = Equipment.BARBELL,
                    difficulty = Difficulty.BEGINNER
                ),
                weight = weight,
                reps = reps,
                oneRepMax = newOneRepMax,
                achievedAt = java.time.LocalDateTime.now(),
                recordType = RecordType.HEAVIEST_WEIGHT
            )
            personalRecordDao.insertRecord(record.toEntity())
            true
        } else {
            false
        }
    }
}
