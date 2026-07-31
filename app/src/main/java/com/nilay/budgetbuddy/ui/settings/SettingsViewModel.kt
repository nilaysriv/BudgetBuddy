package com.nilay.budgetbuddy.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nilay.budgetbuddy.data.local.SettingsDataStore
import com.nilay.budgetbuddy.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val authRepository: AuthRepository
) : ViewModel() {

    val darkModeFlow: StateFlow<Boolean?> = settingsDataStore.darkModeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val currencyFlow: StateFlow<String> = settingsDataStore.currencyFlow
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = "INR")

    val colorSchemeFlow: StateFlow<String> = settingsDataStore.colorSchemeFlow
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = "DYNAMIC")

    val profilePicturePathFlow: StateFlow<String?> = settingsDataStore.profilePicturePathFlow
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = null)

    val userNameFlow: StateFlow<String?> = authRepository.userNameFlow
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = null)

    val userEmailFlow: StateFlow<String?> = authRepository.userEmailFlow
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = null)

    fun toggleDarkMode(isDarkMode: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updateDarkMode(isDarkMode)
        }
    }

    fun updateCurrency(currency: String) {
        viewModelScope.launch {
            authRepository.updateCurrency(currency)
        }
    }

    fun updateColorScheme(colorScheme: String) {
        viewModelScope.launch {
            settingsDataStore.updateColorScheme(colorScheme)
        }
    }

    fun updateProfilePicture(path: String) {
        viewModelScope.launch {
            settingsDataStore.updateProfilePicturePath(path)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun exportToCsv() {
        // Stub for CSV Export
    }
}
