package com.gymtrack.app.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class User(
    val id: Long = 0,
    val name: String,
    val email: String = "",
    val dateOfBirth: LocalDate?,
    val gender: Gender,
    val height: Float?, // in cm
    val weight: Float?, // in kg
    val bodyFatPercentage: Float?,
    val experienceLevel: ExperienceLevel,
    val weeklyWorkoutDays: Int,
    val preferredWorkoutStyles: List<WorkoutStyle>,
    val primaryGoal: FitnessGoal,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val profileImageUrl: String? = null,
    val isOnboardingCompleted: Boolean = false,
    // Settings
    val darkModeEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val autoStartTimer: Boolean = true,
    val restTimerDefault: Int = 90,
    val measurementUnit: String = "kg"
)

enum class Gender {
    MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
}

enum class ExperienceLevel {
    BEGINNER,    // 0-6 months
    INTERMEDIATE, // 1-2 years
    ADVANCED     // 3+ years
}

enum class WorkoutStyle {
    BODYBUILDING,
    POWERLIFTING,
    YOGA,
    CALISTHENICS,
    CARDIO,
    HIIT,
    STRENGTH_TRAINING,
    CROSSFIT,
    SPORTS_SPECIFIC
}

enum class FitnessGoal {
    MUSCLE_GAIN,      // "Build Armor"
    FAT_LOSS,        // "Lean & Mean"
    STRENGTH,         // "Peak Performance"
    MAINTENANCE,      // "Health & Longevity"
    FLEXIBILITY,
    ENDURANCE,
    ATHLETIC_PERFORMANCE
}
