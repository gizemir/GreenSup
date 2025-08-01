package com.gizemir.plantapp.presentation.ui.theme

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

@Singleton
class ThemeManager @Inject constructor(
    private val context: Context
) {
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    
    var isDarkMode by mutableStateOf(false)
        private set
    
    val isDarkModeFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }
    
    suspend fun toggleDarkMode() {
        context.dataStore.edit { preferences ->
            val currentMode = preferences[DARK_MODE_KEY] ?: false
            preferences[DARK_MODE_KEY] = !currentMode
            isDarkMode = !currentMode
        }
    }
    
    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
            isDarkMode = enabled
        }
    }
    
    fun updateCurrentMode(enabled: Boolean) {
        isDarkMode = enabled
    }
} 