package com.gribansky.opentracker.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gribansky.opentracker.data.UserPreferences
import com.gribansky.opentracker.data.dataStore
import com.gribansky.opentracker.data.getUserPreferencesFlow
import com.gribansky.opentracker.data.updateUserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    val userPreferences: StateFlow<UserPreferences> = application.dataStore.getUserPreferencesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun updateUsername(username: String) {
        viewModelScope.launch {
            getApplication<Application>().dataStore.updateUserPreferences(username = username)
        }
    }

    fun updatePassword(password: String) {
        viewModelScope.launch {
            getApplication<Application>().dataStore.updateUserPreferences(password = password)
        }
    }

    fun updateServerAddress(serverAddress: String) {
        viewModelScope.launch {
            getApplication<Application>().dataStore.updateUserPreferences(serverAddress = serverAddress)
        }
    }

    fun updateUseWorkTime(useWorkTime: Boolean) {
        viewModelScope.launch {
            getApplication<Application>().dataStore.updateUserPreferences(useWorkTime = useWorkTime)
        }
    }
}