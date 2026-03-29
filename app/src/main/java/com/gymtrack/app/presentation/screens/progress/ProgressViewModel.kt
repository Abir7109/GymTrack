package com.gymtrack.app.presentation.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtrack.app.domain.model.BodyMeasurement
import com.gymtrack.app.domain.model.Workout
import com.gymtrack.app.domain.model.PersonalRecord
import com.gymtrack.app.domain.repository.BodyMeasurementRepository
import com.gymtrack.app.domain.repository.WorkoutRepository
import com.gymtrack.app.domain.repository.PersonalRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProgressUiState(
    val isLoading: Boolean = false,
    val currentWeight: Float? = null,
    val previousWeight: Float? = null,
    val weightChange: Float? = null,
    val currentBodyFat: Float? = null,
    val previousBodyFat: Float? = null,
    val bodyFatChange: Float? = null,
    val showAddMeasurementDialog: Boolean = false,
    val error: String? = null
)

enum class TimeRange(val days: Int, val label: String) {
    WEEK(7, "Week"),
    MONTH(30, "Month"),
    THREE_MONTHS(90, "3 Months"),
    YEAR(365, "Year"),
    ALL(0, "All Time")
}

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val bodyMeasurementRepository: BodyMeasurementRepository,
    private val workoutRepository: WorkoutRepository,
    private val personalRecordRepository: PersonalRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    private val _measurements = MutableStateFlow<List<BodyMeasurement>>(emptyList())
    val measurements: StateFlow<List<BodyMeasurement>> = _measurements.asStateFlow()

    private val _personalRecords = MutableStateFlow<List<PersonalRecord>>(emptyList())
    val personalRecords: StateFlow<List<PersonalRecord>> = _personalRecords.asStateFlow()

    private val _selectedTimeRange = MutableStateFlow(TimeRange.MONTH)
    val selectedTimeRange: StateFlow<TimeRange> = _selectedTimeRange.asStateFlow()

    init {
        loadProgressData()
    }

    private fun loadProgressData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            bodyMeasurementRepository.getAllMeasurements().collect { measurementList ->
                _measurements.value = measurementList
                updateWeightStats(measurementList)
                _uiState.update { it.copy(isLoading = false) }
            }
        }

        viewModelScope.launch {
            workoutRepository.getCompletedWorkouts().collect { workouts ->
                // Handle workouts if needed
            }
        }

        viewModelScope.launch {
            personalRecordRepository.getAllRecords().collect { records ->
                _personalRecords.value = records
            }
        }
    }

    private fun updateWeightStats(measurements: List<BodyMeasurement>) {
        if (measurements.isEmpty()) return
        
        val sorted = measurements.sortedByDescending { it.recordedAt }
        val current = sorted.firstOrNull()
        val previous = sorted.getOrNull(1)
        
        _uiState.update {
            it.copy(
                currentWeight = current?.weight,
                previousWeight = previous?.weight,
                weightChange = current?.weight?.let { curr ->
                    previous?.weight?.let { prev -> curr - prev }
                },
                currentBodyFat = current?.bodyFatPercentage,
                previousBodyFat = previous?.bodyFatPercentage,
                bodyFatChange = current?.bodyFatPercentage?.let { curr ->
                    previous?.bodyFatPercentage?.let { prev -> curr - prev }
                }
            )
        }
    }

    fun updateTimeRange(timeRange: TimeRange) {
        _selectedTimeRange.value = timeRange
    }

    fun showAddMeasurementDialog() {
        _uiState.update { it.copy(showAddMeasurementDialog = true) }
    }

    fun hideAddMeasurementDialog() {
        _uiState.update { it.copy(showAddMeasurementDialog = false) }
    }

    fun addMeasurement(measurement: BodyMeasurement) {
        viewModelScope.launch {
            bodyMeasurementRepository.addMeasurement(measurement)
            hideAddMeasurementDialog()
        }
    }
}
