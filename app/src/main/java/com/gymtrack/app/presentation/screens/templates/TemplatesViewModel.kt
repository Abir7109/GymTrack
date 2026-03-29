package com.gymtrack.app.presentation.screens.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtrack.app.domain.model.Difficulty
import com.gymtrack.app.domain.model.MuscleGroup
import com.gymtrack.app.domain.model.WorkoutTemplate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TemplatesUiState(
    val templates: List<WorkoutTemplate> = emptyList(),
    val isLoading: Boolean = false,
    val selectedTemplate: WorkoutTemplate? = null,
    val error: String? = null
)

@HiltViewModel
class TemplatesViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(TemplatesUiState())
    val uiState: StateFlow<TemplatesUiState> = _uiState.asStateFlow()

    // In-memory templates for now (database implementation would require WorkoutTemplateRepository)
    private val _templates = MutableStateFlow<List<WorkoutTemplate>>(emptyList())

    init {
        loadTemplates()
    }

    private fun loadTemplates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // For now, templates are empty until database implementation
            _templates.value = emptyList()
            _uiState.update { it.copy(templates = _templates.value, isLoading = false) }
        }
    }

    fun createTemplate(name: String, description: String) {
        viewModelScope.launch {
            val template = WorkoutTemplate(
                name = name,
                description = description,
                exercises = emptyList(),
                estimatedDurationMinutes = 60,
                difficulty = Difficulty.INTERMEDIATE,
                muscleGroups = listOf(MuscleGroup.CHEST)
            )
            _templates.value = _templates.value + template
            _uiState.update { it.copy(templates = _templates.value) }
        }
    }

    fun selectTemplate(template: WorkoutTemplate) {
        _uiState.update { it.copy(selectedTemplate = template) }
    }

    fun deleteTemplate(template: WorkoutTemplate) {
        viewModelScope.launch {
            _templates.value = _templates.value.filter { it.id != template.id }
            _uiState.update { it.copy(templates = _templates.value) }
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedTemplate = null) }
    }
}
