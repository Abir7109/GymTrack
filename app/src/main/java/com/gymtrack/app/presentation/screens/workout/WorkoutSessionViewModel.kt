package com.gymtrack.app.presentation.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtrack.app.domain.model.Exercise
import com.gymtrack.app.domain.repository.UserRepository
import com.gymtrack.app.domain.repository.WorkoutRepository
import com.gymtrack.app.util.CalorieCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutSetLog(
    val setNumber: Int,
    val reps: Int,
    val weight: Float,
    val isCompleted: Boolean = false
)

data class WorkoutSessionUiState(
    val exercise: Exercise? = null,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val elapsedSeconds: Int = 0,
    val calories: Float = 0f,
    val currentSet: Int = 1,
    val totalSets: Int = 4,
    val currentReps: Int = 0,
    val targetReps: Int = 12,
    val currentWeight: Float = 0f,
    val setLogs: List<WorkoutSetLog> = emptyList(),
    val isResting: Boolean = false,
    val restSecondsRemaining: Int = 0,
    val restDuration: Int = 60,
    val showFinishDialog: Boolean = false,
    val workoutPhase: WorkoutPhase = WorkoutPhase.PRE_START,
    val userWeight: Float = 70f
)

enum class WorkoutPhase {
    PRE_START, ACTIVE, REST, FINISHED
}

@HiltViewModel
class WorkoutSessionViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutSessionUiState())
    val uiState: StateFlow<WorkoutSessionUiState> = _uiState.asStateFlow()

    private var workoutId: Long? = null
    private var timerJob: Job? = null
    private var restTimerJob: Job? = null

    fun setExercise(exercise: Exercise) {
        viewModelScope.launch {
            val user = userRepository.getCurrentUserOnce()
            val weight = user?.weight ?: 70f
            _uiState.value = _uiState.value.copy(
                exercise = exercise,
                workoutPhase = WorkoutPhase.PRE_START,
                targetReps = 12,
                totalSets = 4,
                restDuration = 60,
                userWeight = weight
            )
        }
    }

    fun setUserWeight(weight: Float) {
        _uiState.value = _uiState.value.copy(userWeight = weight.coerceIn(30f, 300f))
    }

    fun startWorkout() {
        val exercise = _uiState.value.exercise ?: return
        val state = _uiState.value
        viewModelScope.launch {
            val id = workoutRepository.startWorkout(exercise.name)
            workoutId = id
            _uiState.value = state.copy(
                isRunning = true,
                isPaused = false,
                workoutPhase = WorkoutPhase.ACTIVE,
                currentReps = state.targetReps
            )
            startTimer()
        }
    }

    fun pauseWorkout() {
        _uiState.value = _uiState.value.copy(isRunning = false, isPaused = true)
        timerJob?.cancel()
    }

    fun resumeWorkout() {
        _uiState.value = _uiState.value.copy(isRunning = true, isPaused = false)
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.isRunning) {
                delay(1000L)
                val state = _uiState.value
                val newSeconds = state.elapsedSeconds + 1
                val minutes = newSeconds / 60f
                val met = CalorieCalculator.getMetForExercise(
                    state.exercise?.name ?: "",
                    state.exercise?.muscleGroup
                )
                val calories = CalorieCalculator.calculateCalories(met, state.userWeight, minutes)
                _uiState.value = state.copy(
                    elapsedSeconds = newSeconds,
                    calories = calories
                )
            }
        }
    }

    fun updateReps(reps: Int) {
        _uiState.value = _uiState.value.copy(currentReps = reps.coerceAtLeast(0))
    }

    fun updateWeight(weight: Float) {
        _uiState.value = _uiState.value.copy(currentWeight = weight.coerceAtLeast(0f))
    }

    fun updateTargetReps(reps: Int) {
        _uiState.value = _uiState.value.copy(targetReps = reps.coerceAtLeast(1))
    }

    fun updateTotalSets(sets: Int) {
        _uiState.value = _uiState.value.copy(totalSets = sets.coerceAtLeast(1))
    }

    fun updateRestDuration(seconds: Int) {
        _uiState.value = _uiState.value.copy(restDuration = seconds.coerceAtLeast(10))
    }

    fun completeSet() {
        val state = _uiState.value
        val newLog = WorkoutSetLog(
            setNumber = state.currentSet,
            reps = state.currentReps,
            weight = state.currentWeight,
            isCompleted = true
        )
        val updatedLogs = state.setLogs + newLog

        if (state.currentSet >= state.totalSets) {
            _uiState.value = state.copy(
                setLogs = updatedLogs,
                isRunning = false,
                isPaused = true
            )
            timerJob?.cancel()
        } else {
            _uiState.value = state.copy(
                setLogs = updatedLogs,
                currentSet = state.currentSet + 1,
                currentReps = state.targetReps,
                isRunning = false,
                workoutPhase = WorkoutPhase.REST,
                isResting = true,
                restSecondsRemaining = state.restDuration
            )
            timerJob?.cancel()
            startRestTimer()
        }
    }

    fun addExtraSet() {
        val state = _uiState.value
        _uiState.value = state.copy(
            totalSets = state.totalSets + 1,
            currentSet = state.currentSet + 1,
            currentReps = state.targetReps,
            isRunning = true,
            isPaused = false,
            workoutPhase = WorkoutPhase.REST,
            isResting = true,
            restSecondsRemaining = state.restDuration
        )
        startRestTimer()
    }

    private fun startRestTimer() {
        restTimerJob?.cancel()
        restTimerJob = viewModelScope.launch {
            while (_uiState.value.restSecondsRemaining > 0) {
                delay(1000L)
                val state = _uiState.value
                val newElapsed = state.elapsedSeconds + 1
                val minutes = newElapsed / 60f
                val met = CalorieCalculator.getMetForExercise(
                    state.exercise?.name ?: "",
                    state.exercise?.muscleGroup
                )
                _uiState.value = state.copy(
                    restSecondsRemaining = state.restSecondsRemaining - 1,
                    elapsedSeconds = newElapsed,
                    calories = CalorieCalculator.calculateCalories(met, state.userWeight, minutes)
                )
            }
            _uiState.value = _uiState.value.copy(
                isResting = false,
                isRunning = true,
                workoutPhase = WorkoutPhase.ACTIVE
            )
            startTimer()
        }
    }

    fun skipRest() {
        restTimerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isResting = false,
            isRunning = true,
            restSecondsRemaining = 0,
            workoutPhase = WorkoutPhase.ACTIVE
        )
        startTimer()
    }

    fun showFinishDialog() {
        _uiState.value = _uiState.value.copy(showFinishDialog = true)
    }

    fun dismissFinishDialog() {
        _uiState.value = _uiState.value.copy(showFinishDialog = false)
    }

    fun finishWorkout(onFinished: () -> Unit) {
        timerJob?.cancel()
        restTimerJob?.cancel()
        viewModelScope.launch {
            val state = _uiState.value
            val id = workoutId
            if (id != null) {
                val minutes = state.elapsedSeconds / 60
                val totalVolume = state.setLogs.sumOf { (it.weight * it.reps).toDouble() }.toFloat()
                val totalReps = state.setLogs.sumOf { it.reps }
                workoutRepository.completeWorkout(
                    workoutId = id,
                    durationMinutes = minutes,
                    totalVolume = totalVolume,
                    totalSets = state.setLogs.size,
                    totalReps = totalReps
                )
            }
            onFinished()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        restTimerJob?.cancel()
    }
}
