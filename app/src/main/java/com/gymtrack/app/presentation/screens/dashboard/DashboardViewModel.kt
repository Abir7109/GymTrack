package com.gymtrack.app.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtrack.app.domain.model.*
import com.gymtrack.app.domain.repository.ExerciseRepository
import com.gymtrack.app.domain.repository.UserRepository
import com.gymtrack.app.domain.repository.WorkoutRepository
import com.gymtrack.app.util.CalorieCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val recentWorkouts: List<Workout> = emptyList(),
    val workoutsThisWeek: Int = 0,
    val totalMinutesThisWeek: Int = 0,
    val currentStreak: Int = 0,
    val totalWorkouts: Int = 0,
    val totalCalories: Float = 0f,
    val suggestedExercises: List<Exercise> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            try {
                userRepository.getCurrentUser().collect { user ->
                    _uiState.value = _uiState.value.copy(user = user)
                    if (user != null) {
                        loadSuggestions(user)
                    }
                }
            } catch (_: Exception) {}
        }

        viewModelScope.launch {
            try {
                workoutRepository.getCompletedWorkouts().collect { workouts ->
                    val weekAgo = LocalDateTime.now().minusDays(7)
                    val recentCompleted = workouts.filter { it.completedAt != null && it.completedAt!! >= weekAgo }
                    val user = userRepository.getCurrentUserOnce()
                    val weight = user?.weight ?: 70f
                    val streak = calculateStreak()

                    _uiState.value = _uiState.value.copy(
                        recentWorkouts = workouts.take(5),
                        workoutsThisWeek = recentCompleted.size,
                        totalMinutesThisWeek = recentCompleted.sumOf { it.durationMinutes },
                        totalWorkouts = workouts.size,
                        currentStreak = streak,
                        totalCalories = workouts.sumOf { workout ->
                            val met = CalorieCalculator.getMetForExercise(workout.name)
                            CalorieCalculator.calculateCalories(met, weight, workout.durationMinutes.toFloat()).toDouble()
                        }.toFloat(),
                        isLoading = false
                    )
                }
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private suspend fun calculateStreak(): Int {
        val ninetyDaysAgo = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000)
        val completedDates = workoutRepository.getCompletedWorkoutDatesSince(ninetyDaysAgo)
        if (completedDates.isEmpty()) return 0

        val zone = ZoneId.systemDefault()
        val workoutDays = completedDates.map {
            LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000))
        }.distinct().sortedDescending()

        if (workoutDays.isEmpty()) return 0

        var streak = 0
        var checkDate = LocalDate.now()

        // Check if there's a workout today or yesterday to start the streak
        if (workoutDays.contains(checkDate)) {
            streak = 1
            checkDate = checkDate.minusDays(1)
        } else if (workoutDays.contains(checkDate.minusDays(1))) {
            streak = 1
            checkDate = checkDate.minusDays(2)
        } else {
            return 0
        }

        // Count consecutive days
        while (workoutDays.contains(checkDate)) {
            streak++
            checkDate = checkDate.minusDays(1)
        }

        return streak
    }

    private fun loadSuggestions(user: User) {
        viewModelScope.launch {
            try {
                val allExercises = exerciseRepository.getAllExercises().first()
                val suggested = when (user.primaryGoal) {
                    FitnessGoal.MUSCLE_GAIN -> allExercises.filter {
                        it.equipment == Equipment.BARBELL || it.equipment == Equipment.DUMBBELL
                    }.take(5)
                    FitnessGoal.FAT_LOSS -> allExercises.filter {
                        it.muscleGroup == MuscleGroup.CARDIO || it.equipment == Equipment.BODYWEIGHT
                    }.take(5)
                    FitnessGoal.STRENGTH -> allExercises.filter {
                        it.difficulty == Difficulty.ADVANCED || it.difficulty == Difficulty.INTERMEDIATE
                    }.take(5)
                    FitnessGoal.ENDURANCE -> allExercises.filter {
                        it.muscleGroup == MuscleGroup.CARDIO || it.muscleGroup == MuscleGroup.FULL_BODY
                    }.take(5)
                    FitnessGoal.FLEXIBILITY -> allExercises.filter {
                        it.equipment == Equipment.BODYWEIGHT
                    }.take(5)
                    else -> allExercises.take(5)
                }
                _uiState.value = _uiState.value.copy(suggestedExercises = if (suggested.isNotEmpty()) suggested else allExercises.take(5))
            } catch (_: Exception) {
                try {
                    val all = exerciseRepository.getAllExercises().first()
                    _uiState.value = _uiState.value.copy(suggestedExercises = all.take(5))
                } catch (_: Exception) {}
            }
        }
    }

    fun refresh() {
        loadDashboardData()
    }
}
