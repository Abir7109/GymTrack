package com.gymtrack.app.util

import com.gymtrack.app.domain.model.MuscleGroup

object CalorieCalculator {

    private val MET_VALUES: Map<String, Float> = mapOf(
        // Chest
        "Barbell Bench Press" to 6.0f,
        "Incline Dumbbell Press" to 5.0f,
        "Cable Fly" to 3.5f,
        "Push-ups" to 8.0f,

        // Back
        "Pull-ups" to 8.0f,
        "Barbell Row" to 6.0f,
        "Lat Pulldown" to 5.0f,
        "Deadlift" to 6.0f,

        // Shoulders
        "Overhead Press" to 6.0f,
        "Lateral Raise" to 4.0f,

        // Biceps
        "Barbell Curl" to 5.0f,
        "Dumbbell Curl" to 5.0f,

        // Triceps
        "Tricep Pushdown" to 4.0f,
        "Dips" to 8.0f,

        // Quadriceps
        "Barbell Squat" to 8.0f,
        "Leg Press" to 6.0f,
        "Lunges" to 8.0f,

        // Hamstrings
        "Romanian Deadlift" to 6.0f,
        "Leg Curl" to 4.0f,

        // Glutes
        "Hip Thrust" to 6.0f,

        // Core
        "Plank" to 3.0f,
        "Crunches" to 3.8f,

        // Cardio / General
        "Running (10 km/h)" to 10.0f,
        "Jump Rope (Fast)" to 12.0f,
        "Brisk Walking" to 3.5f,
        "Yoga (Hatha)" to 2.5f,
        "Circuit Training" to 8.0f
    )

    private val MUSCLE_GROUP_DEFAULT_MET: Map<MuscleGroup, Float> = mapOf(
        MuscleGroup.CHEST to 5.0f,
        MuscleGroup.BACK to 6.0f,
        MuscleGroup.SHOULDERS to 5.0f,
        MuscleGroup.BICEPS to 5.0f,
        MuscleGroup.TRICEPS to 5.0f,
        MuscleGroup.FOREARMS to 4.0f,
        MuscleGroup.CORE to 3.5f,
        MuscleGroup.QUADRICEPS to 7.0f,
        MuscleGroup.HAMSTRINGS to 6.0f,
        MuscleGroup.GLUTES to 6.0f,
        MuscleGroup.CALVES to 5.0f,
        MuscleGroup.FULL_BODY to 8.0f,
        MuscleGroup.CARDIO to 10.0f
    )

    private const val DEFAULT_MET = 6.0f

    fun getMetForExercise(exerciseName: String, muscleGroup: MuscleGroup? = null): Float {
        MET_VALUES[exerciseName]?.let { return it }
        if (muscleGroup != null) {
            MUSCLE_GROUP_DEFAULT_MET[muscleGroup]?.let { return it }
        }
        return DEFAULT_MET
    }

    fun calculateCalories(metValue: Float, weightKg: Float, timeMinutes: Float): Float {
        return timeMinutes * (metValue * 3.5f * weightKg) / 200f
    }

    fun calculateCaloriesPerMinute(metValue: Float, weightKg: Float): Float {
        return (metValue * 3.5f * weightKg) / 200f
    }
}
