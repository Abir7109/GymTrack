package com.gymtrack.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gymtrack_settings")

/**
 * DataStore manager for user preferences.
 * Handles dark mode, notifications, timer settings, and measurement units.
 */
@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val AUTO_START_TIMER = booleanPreferencesKey("auto_start_timer")
        val REST_TIMER_DEFAULT = intPreferencesKey("rest_timer_default")
        val MEASUREMENT_UNIT = stringPreferencesKey("measurement_unit")
        val LAST_BACKUP_DATE = longPreferencesKey("last_backup_date")
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
    }

    // Dark Mode
    val darkModeFlow: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[DARK_MODE] ?: false
        }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE] = enabled
        }
    }

    // Notifications
    val notificationEnabledFlow: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[NOTIFICATIONS_ENABLED] ?: true
        }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    // Auto Start Timer
    val autoStartTimerFlow: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[AUTO_START_TIMER] ?: true
        }

    suspend fun setAutoStartTimer(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_START_TIMER] = enabled
        }
    }

    // Rest Timer Default
    val restTimerDefaultFlow: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[REST_TIMER_DEFAULT] ?: 90
        }

    suspend fun setRestTimerDefault(seconds: Int) {
        dataStore.edit { preferences ->
            preferences[REST_TIMER_DEFAULT] = seconds
        }
    }

    // Measurement Unit
    val measurementUnitFlow: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[MEASUREMENT_UNIT] ?: "kg"
        }

    suspend fun setMeasurementUnit(unit: String) {
        dataStore.edit { preferences ->
            preferences[MEASUREMENT_UNIT] = unit
        }
    }

    // Last Backup Date
    val lastBackupDateFlow: Flow<Long> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[LAST_BACKUP_DATE] ?: 0L
        }

    suspend fun setLastBackupDate(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[LAST_BACKUP_DATE] = timestamp
        }
    }

    // First Launch
    val isFirstLaunchFlow: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[IS_FIRST_LAUNCH] ?: true
        }

    suspend fun setFirstLaunchCompleted() {
        dataStore.edit { preferences ->
            preferences[IS_FIRST_LAUNCH] = false
        }
    }

    // Clear all preferences
    suspend fun clearAllPreferences() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
