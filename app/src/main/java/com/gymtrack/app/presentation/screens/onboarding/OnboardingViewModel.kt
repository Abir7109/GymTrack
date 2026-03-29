package com.gymtrack.app.presentation.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtrack.app.domain.model.Exercise
import com.gymtrack.app.domain.model.User
import com.gymtrack.app.domain.model.ExperienceLevel
import com.gymtrack.app.domain.model.FitnessGoal
import com.gymtrack.app.domain.model.Gender
import com.gymtrack.app.domain.model.WorkoutStyle
import com.gymtrack.app.domain.repository.ExerciseRepository
import com.gymtrack.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class OnboardingUiState(
    val currentStep: Int = 0,
    val name: String = "",
    val email: String = "",
    val gender: Gender = Gender.MALE,
    val dateOfBirth: LocalDate? = null,
    val height: Float? = null,
    val weight: Float? = null,
    val experienceLevel: ExperienceLevel = ExperienceLevel.BEGINNER,
    val weeklyWorkoutDays: Int = 3,
    val preferredWorkoutStyles: List<WorkoutStyle> = emptyList(),
    val primaryGoal: FitnessGoal = FitnessGoal.MUSCLE_GAIN,
    val isLoading: Boolean = false,
    val isCompleted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isOnboardingDone = MutableStateFlow<Boolean?>(null)
    val isOnboardingDone: StateFlow<Boolean?> = _isOnboardingDone.asStateFlow()

    val user = userRepository.getCurrentUser()

    init {
        checkExistingUser()
    }

    private fun checkExistingUser() {
        viewModelScope.launch {
            val existingUser = userRepository.getCurrentUserOnce()
            if (existingUser != null) {
                _uiState.value = _uiState.value.copy(
                    name = existingUser.name,
                    email = existingUser.email,
                    gender = existingUser.gender,
                    dateOfBirth = existingUser.dateOfBirth,
                    height = existingUser.height,
                    weight = existingUser.weight,
                    experienceLevel = existingUser.experienceLevel,
                    weeklyWorkoutDays = existingUser.weeklyWorkoutDays,
                    preferredWorkoutStyles = existingUser.preferredWorkoutStyles,
                    primaryGoal = existingUser.primaryGoal,
                    isCompleted = existingUser.isOnboardingCompleted
                )
                _isOnboardingDone.value = existingUser.isOnboardingCompleted
            } else {
                _isOnboardingDone.value = false
            }
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun updateGender(gender: Gender) {
        _uiState.value = _uiState.value.copy(gender = gender)
    }

    fun updateDateOfBirth(date: LocalDate?) {
        _uiState.value = _uiState.value.copy(dateOfBirth = date)
    }

    fun updateHeight(height: Float?) {
        _uiState.value = _uiState.value.copy(height = height)
    }

    fun updateWeight(weight: Float?) {
        _uiState.value = _uiState.value.copy(weight = weight)
    }

    fun updateExperienceLevel(level: ExperienceLevel) {
        _uiState.value = _uiState.value.copy(experienceLevel = level)
    }

    fun updateWeeklyWorkoutDays(days: Int) {
        _uiState.value = _uiState.value.copy(weeklyWorkoutDays = days)
    }

    fun updateWorkoutStyles(styles: List<WorkoutStyle>) {
        _uiState.value = _uiState.value.copy(preferredWorkoutStyles = styles)
    }

    fun updatePrimaryGoal(goal: FitnessGoal) {
        _uiState.value = _uiState.value.copy(primaryGoal = goal)
    }

    fun nextStep() {
        _uiState.value = _uiState.value.copy(currentStep = _uiState.value.currentStep + 1)
    }

    fun previousStep() {
        if (_uiState.value.currentStep > 0) {
            _uiState.value = _uiState.value.copy(currentStep = _uiState.value.currentStep - 1)
        }
    }

    fun saveOnboarding(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val state = _uiState.value
                val user = User(
                    name = state.name,
                    email = state.email,
                    dateOfBirth = state.dateOfBirth,
                    gender = state.gender,
                    height = state.height,
                    weight = state.weight,
                    bodyFatPercentage = null,
                    experienceLevel = state.experienceLevel,
                    weeklyWorkoutDays = state.weeklyWorkoutDays,
                    preferredWorkoutStyles = state.preferredWorkoutStyles,
                    primaryGoal = state.primaryGoal,
                    isOnboardingCompleted = true
                )
                
                userRepository.saveUser(user)
                exerciseRepository.seedDefaultExercises()
                
                _uiState.value = _uiState.value.copy(isLoading = false, isCompleted = true)
                onComplete()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }
}
