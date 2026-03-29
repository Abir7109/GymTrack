package com.gymtrack.app.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtrack.app.data.repository.DataStoreManager
import com.gymtrack.app.domain.model.User
import com.gymtrack.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileSettings(
    val darkModeEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val autoStartTimer: Boolean = true,
    val restTimerDefault: Int = 90,
    val measurementUnit: String = "kg"
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _settings = MutableStateFlow(ProfileSettings())
    val settings: StateFlow<ProfileSettings> = _settings.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            // Combine user data with DataStore preferences
            // First combine: user + dark mode
            val userAndDarkMode = combine(
                userRepository.getCurrentUser(),
                dataStoreManager.darkModeFlow
            ) { userData, darkMode ->
                Pair(userData, darkMode)
            }
            
            // Second combine: + notifications
            val userDarkNotify = combine(
                userAndDarkMode,
                dataStoreManager.notificationEnabledFlow
            ) { pair, notifications ->
                Triple(pair.first, pair.second, notifications)
            }
            
            // Third combine: + auto timer
            val userDarkNotifyTimer = combine(
                userDarkNotify,
                dataStoreManager.autoStartTimerFlow
            ) { triple, autoTimer ->
                Quartet(triple.first, triple.second, triple.third, autoTimer)
            }
            
            // Fourth combine: + rest timer
            val userDarkNotifyTimerRest = combine(
                userDarkNotifyTimer,
                dataStoreManager.restTimerDefaultFlow
            ) { quartet, restTimer ->
                Quintet(quartet.first, quartet.second, quartet.third, quartet.fourth, restTimer)
            }
            
            // Final combine: + measurement unit
            combine(
                userDarkNotifyTimerRest,
                dataStoreManager.measurementUnitFlow
            ) { quintet, unit ->
                _user.value = quintet.first
                ProfileSettings(
                    darkModeEnabled = quintet.second,
                    notificationsEnabled = quintet.third,
                    autoStartTimer = quintet.fourth,
                    restTimerDefault = quintet.fifth,
                    measurementUnit = unit
                )
            }.collect { settings ->
                _settings.value = settings
            }
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            userRepository.updateUser(user)
        }
    }

    fun updateDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setDarkMode(enabled)
        }
    }

    fun updateNotifications(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setNotificationEnabled(enabled)
        }
    }

    fun updateAutoStartTimer(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setAutoStartTimer(enabled)
        }
    }

    fun updateMeasurementUnit(unit: String) {
        viewModelScope.launch {
            dataStoreManager.setMeasurementUnit(unit)
        }
    }

    fun updateRestTimerDefault(seconds: Int) {
        viewModelScope.launch {
            dataStoreManager.setRestTimerDefault(seconds)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            userRepository.deleteUser()
            dataStoreManager.clearAllPreferences()
        }
    }
}

// Helper data classes for combine
private data class Quartet<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
private data class Quintet<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)
