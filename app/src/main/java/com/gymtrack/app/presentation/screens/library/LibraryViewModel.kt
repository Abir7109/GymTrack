package com.gymtrack.app.presentation.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtrack.app.domain.model.Exercise
import com.gymtrack.app.domain.model.MuscleGroup
import com.gymtrack.app.domain.model.Equipment
import com.gymtrack.app.domain.model.Difficulty
import com.gymtrack.app.domain.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val exercises: List<Exercise> = emptyList(),
    val filteredExercises: List<Exercise> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedMuscleGroup: MuscleGroup? = null,
    val selectedEquipment: Equipment? = null,
    val selectedDifficulty: Difficulty? = null,
    val showFavoritesOnly: Boolean = false,
    val selectedExercise: Exercise? = null,
    val error: String? = null
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadExercises()
    }

    private fun loadExercises() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Seed default exercises if needed
            exerciseRepository.seedDefaultExercises()
            exerciseRepository.getAllExercises().collect { exerciseList ->
                _uiState.update { 
                    it.copy(
                        exercises = exerciseList,
                        filteredExercises = exerciseList,
                        isLoading = false
                    ) 
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        filterExercises()
    }

    fun toggleFavoriteOnly() {
        _uiState.update { it.copy(showFavoritesOnly = !it.showFavoritesOnly) }
        filterExercises()
    }

    fun updateSelectedMuscleGroup(muscleGroup: MuscleGroup?) {
        _uiState.update { it.copy(selectedMuscleGroup = muscleGroup) }
        filterExercises()
    }

    fun updateSelectedEquipment(equipment: Equipment?) {
        _uiState.update { it.copy(selectedEquipment = equipment) }
        filterExercises()
    }

    fun updateSelectedDifficulty(difficulty: Difficulty?) {
        _uiState.update { it.copy(selectedDifficulty = difficulty) }
        filterExercises()
    }

    fun selectExercise(exercise: Exercise) {
        _uiState.update { it.copy(selectedExercise = exercise) }
    }

    fun clearSelectedExercise() {
        _uiState.update { it.copy(selectedExercise = null) }
    }

    fun toggleFavorite(exerciseId: Long) {
        viewModelScope.launch {
            val exercise = _uiState.value.exercises.find { it.id == exerciseId }
            exercise?.let {
                exerciseRepository.toggleFavorite(exerciseId, !it.isFavorite)
            }
        }
    }

    private fun filterExercises() {
        val state = _uiState.value
        val filtered = state.exercises.filter { exercise ->
            val matchesQuery = state.searchQuery.isEmpty() || 
                exercise.name.contains(state.searchQuery, ignoreCase = true)
            val matchesMuscle = state.selectedMuscleGroup == null || 
                exercise.muscleGroup == state.selectedMuscleGroup
            val matchesEquipment = state.selectedEquipment == null || 
                exercise.equipment == state.selectedEquipment
            val matchesDifficulty = state.selectedDifficulty == null || 
                exercise.difficulty == state.selectedDifficulty
            val matchesFavorite = !state.showFavoritesOnly || exercise.isFavorite
            
            matchesQuery && matchesMuscle && matchesEquipment && matchesDifficulty && matchesFavorite
        }
        _uiState.update { it.copy(filteredExercises = filtered) }
    }
}
