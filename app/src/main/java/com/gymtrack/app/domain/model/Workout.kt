package com.gymtrack.app.domain.model

import java.time.LocalDateTime

data class Exercise(
    val id: Long = 0,
    val name: String,
    val description: String,
    val instructions: String,
    val muscleGroup: MuscleGroup,
    val secondaryMuscles: List<MuscleGroup> = emptyList(),
    val equipment: Equipment,
    val difficulty: Difficulty,
    val isCustom: Boolean = false,
    val isFavorite: Boolean = false,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val calorieBurnRate: Float = 0f
)

enum class MuscleGroup {
    CHEST,
    BACK,
    SHOULDERS,
    BICEPS,
    TRICEPS,
    FOREARMS,
    CORE,
    QUADRICEPS,
    HAMSTRINGS,
    GLUTES,
    CALVES,
    FULL_BODY,
    CARDIO
}

enum class Equipment {
    BARBELL,
    DUMBBELL,
    KETTLEBELL,
    CABLE,
    MACHINE,
    BODYWEIGHT,
    RESISTANCE_BAND,
    EZ_BAR,
    SMITH_MACHINE,
    LEG_PRESS,
    OTHER,
    NONE_REQUIRED
}

enum class Difficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

data class Workout(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val workoutExercises: List<WorkoutExercise> = emptyList(),
    val scheduledDate: LocalDateTime? = null,
    val startedAt: LocalDateTime? = null,
    val completedAt: LocalDateTime? = null,
    val durationMinutes: Int = 0,
    val totalVolume: Float = 0f,
    val totalSets: Int = 0,
    val totalReps: Int = 0,
    val status: WorkoutStatus = WorkoutStatus.PLANNED,
    val notes: String = ""
)

enum class WorkoutStatus {
    PLANNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    SKIPPED
}

data class WorkoutExercise(
    val id: Long = 0,
    val exercise: Exercise,
    val sets: List<ExerciseSet> = emptyList(),
    val restTimeSeconds: Int = 90,
    val notes: String = "",
    val orderIndex: Int = 0
)

data class ExerciseSet(
    val id: Long = 0,
    val setNumber: Int,
    val weight: Float, // in kg
    val reps: Int,
    val isCompleted: Boolean = false,
    val isWarmup: Boolean = false,
    val isDropSet: Boolean = false,
    val isSuperset: Boolean = false,
    val rpe: Float? = null, // Rate of Perceived Exertion (1-10)
    val completedAt: LocalDateTime? = null,
    val notes: String = ""
)

data class PersonalRecord(
    val id: Long = 0,
    val exercise: Exercise,
    val weight: Float,
    val reps: Int,
    val oneRepMax: Float,
    val achievedAt: LocalDateTime,
    val recordType: RecordType
)

enum class RecordType {
    HEAVIEST_WEIGHT,
    MOST_REPS,
    HIGHEST_VOLUME,
    FIRST_LIFT
}

data class BodyMeasurement(
    val id: Long = 0,
    val recordedAt: LocalDateTime,
    val weight: Float?,
    val bodyFatPercentage: Float?,
    val muscleMass: Float?,
    val waterPercentage: Float?,
    val chest: Float?,
    val waist: Float?,
    val hips: Float?,
    val biceps: Float?,
    val thighs: Float?,
    val calves: Float?,
    val neck: Float?,
    val shoulders: Float?,
    val notes: String = ""
)

data class WorkoutTemplate(
    val id: Long = 0,
    val name: String,
    val description: String,
    val exercises: List<TemplateExercise> = emptyList(),
    val estimatedDurationMinutes: Int,
    val difficulty: Difficulty,
    val muscleGroups: List<MuscleGroup>
)

data class TemplateExercise(
    val exercise: Exercise,
    val targetSets: Int,
    val targetReps: String, // "8-12" or "10" or "AMRAP"
    val restSeconds: Int,
    val notes: String = ""
)
