package com.gymtrack.app.data.mapper

import com.gymtrack.app.data.local.entity.*
import com.gymtrack.app.domain.model.*
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

// User Mappers
fun UserEntity.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    dateOfBirth = dateOfBirth?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() },
    gender = Gender.valueOf(gender),
    height = height,
    weight = weight,
    bodyFatPercentage = bodyFatPercentage,
    experienceLevel = ExperienceLevel.valueOf(experienceLevel),
    weeklyWorkoutDays = weeklyWorkoutDays,
    preferredWorkoutStyles = preferredWorkoutStyles.split(",").filter { it.isNotBlank() }.map { WorkoutStyle.valueOf(it) },
    primaryGoal = FitnessGoal.valueOf(primaryGoal),
    createdAt = Instant.ofEpochMilli(createdAt).atZone(ZoneId.systemDefault()).toLocalDateTime(),
    profileImageUrl = profileImageUrl,
    isOnboardingCompleted = isOnboardingCompleted,
    darkModeEnabled = darkModeEnabled,
    notificationsEnabled = notificationsEnabled,
    autoStartTimer = autoStartTimer,
    restTimerDefault = restTimerDefault,
    measurementUnit = measurementUnit
)

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    name = name,
    email = email,
    dateOfBirth = dateOfBirth?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
    gender = gender.name,
    height = height,
    weight = weight,
    bodyFatPercentage = bodyFatPercentage,
    experienceLevel = experienceLevel.name,
    weeklyWorkoutDays = weeklyWorkoutDays,
    preferredWorkoutStyles = preferredWorkoutStyles.joinToString(",") { it.name },
    primaryGoal = primaryGoal.name,
    createdAt = createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    profileImageUrl = profileImageUrl,
    isOnboardingCompleted = isOnboardingCompleted,
    darkModeEnabled = darkModeEnabled,
    notificationsEnabled = notificationsEnabled,
    autoStartTimer = autoStartTimer,
    restTimerDefault = restTimerDefault,
    measurementUnit = measurementUnit
)

// Exercise Mappers
fun ExerciseEntity.toDomain(): Exercise = Exercise(
    id = id,
    name = name,
    description = description,
    instructions = instructions,
    muscleGroup = MuscleGroup.valueOf(muscleGroup),
    secondaryMuscles = secondaryMuscles.split(",").filter { it.isNotBlank() }.map { MuscleGroup.valueOf(it) },
    equipment = Equipment.valueOf(equipment),
    difficulty = Difficulty.valueOf(difficulty),
    isCustom = isCustom,
    isFavorite = isFavorite,
    imageUrl = imageUrl,
    videoUrl = videoUrl,
    calorieBurnRate = calorieBurnRate
)

fun Exercise.toEntity(): ExerciseEntity = ExerciseEntity(
    id = id,
    name = name,
    description = description,
    instructions = instructions,
    muscleGroup = muscleGroup.name,
    secondaryMuscles = secondaryMuscles.joinToString(",") { it.name },
    equipment = equipment.name,
    difficulty = difficulty.name,
    isCustom = isCustom,
    isFavorite = isFavorite,
    imageUrl = imageUrl,
    videoUrl = videoUrl,
    calorieBurnRate = calorieBurnRate
)

// Workout Mappers
fun WorkoutEntity.toDomain(): Workout = Workout(
    id = id,
    name = name,
    description = description,
    workoutExercises = emptyList(), // Will be populated separately
    scheduledDate = scheduledDate?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime() },
    startedAt = startedAt?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime() },
    completedAt = completedAt?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime() },
    durationMinutes = durationMinutes,
    totalVolume = totalVolume,
    totalSets = totalSets,
    totalReps = totalReps,
    status = WorkoutStatus.valueOf(status),
    notes = notes
)

fun Workout.toEntity(): WorkoutEntity = WorkoutEntity(
    id = id,
    name = name,
    description = description,
    scheduledDate = scheduledDate?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
    startedAt = startedAt?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
    completedAt = completedAt?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
    durationMinutes = durationMinutes,
    totalVolume = totalVolume,
    totalSets = totalSets,
    totalReps = totalReps,
    status = status.name,
    notes = notes
)

// ExerciseSet Mappers
fun ExerciseSetEntity.toDomain(): ExerciseSet = ExerciseSet(
    id = id,
    setNumber = setNumber,
    weight = weight,
    reps = reps,
    isCompleted = isCompleted,
    isWarmup = isWarmup,
    isDropSet = isDropSet,
    isSuperset = isSuperset,
    rpe = rpe,
    completedAt = completedAt?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime() },
    notes = notes
)

fun ExerciseSet.toEntity(workoutExerciseId: Long): ExerciseSetEntity = ExerciseSetEntity(
    id = id,
    workoutExerciseId = workoutExerciseId,
    setNumber = setNumber,
    weight = weight,
    reps = reps,
    isCompleted = isCompleted,
    isWarmup = isWarmup,
    isDropSet = isDropSet,
    isSuperset = isSuperset,
    rpe = rpe,
    completedAt = completedAt?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
    notes = notes
)

// PersonalRecord Mappers
fun PersonalRecordEntity.toDomain(exercise: Exercise): PersonalRecord = PersonalRecord(
    id = id,
    exercise = exercise,
    weight = weight,
    reps = reps,
    oneRepMax = oneRepMax,
    achievedAt = Instant.ofEpochMilli(achievedAt).atZone(ZoneId.systemDefault()).toLocalDateTime(),
    recordType = RecordType.valueOf(recordType)
)

fun PersonalRecordEntity.toDomain(): PersonalRecord = PersonalRecord(
    id = id,
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
    oneRepMax = oneRepMax,
    achievedAt = Instant.ofEpochMilli(achievedAt).atZone(ZoneId.systemDefault()).toLocalDateTime(),
    recordType = RecordType.valueOf(recordType)
)

fun PersonalRecord.toEntity(): PersonalRecordEntity = PersonalRecordEntity(
    id = id,
    exerciseId = exercise.id,
    weight = weight,
    reps = reps,
    oneRepMax = oneRepMax,
    achievedAt = achievedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    recordType = recordType.name
)

// BodyMeasurement Mappers
fun BodyMeasurementEntity.toDomain(): BodyMeasurement = BodyMeasurement(
    id = id,
    recordedAt = Instant.ofEpochMilli(recordedAt).atZone(ZoneId.systemDefault()).toLocalDateTime(),
    weight = weight,
    bodyFatPercentage = bodyFatPercentage,
    muscleMass = muscleMass,
    waterPercentage = waterPercentage,
    chest = chest,
    waist = waist,
    hips = hips,
    biceps = biceps,
    thighs = thighs,
    calves = calves,
    neck = neck,
    shoulders = shoulders,
    notes = notes
)

fun BodyMeasurement.toEntity(): BodyMeasurementEntity = BodyMeasurementEntity(
    id = id,
    recordedAt = recordedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    weight = weight,
    bodyFatPercentage = bodyFatPercentage,
    muscleMass = muscleMass,
    waterPercentage = waterPercentage,
    chest = chest,
    waist = waist,
    hips = hips,
    biceps = biceps,
    thighs = thighs,
    calves = calves,
    neck = neck,
    shoulders = shoulders,
    notes = notes
)

// Utility functions for calculations
fun calculateOneRepMax(weight: Float, reps: Int): Float {
    if (reps == 1) return weight
    if (reps <= 0 || weight <= 0) return 0f
    // Epley formula
    return weight * (1 + reps / 30f)
}

fun calculateVolume(weight: Float, reps: Int): Float = weight * reps
