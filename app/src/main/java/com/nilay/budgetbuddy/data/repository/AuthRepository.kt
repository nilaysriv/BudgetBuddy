package com.nilay.budgetbuddy.data.repository

import com.nilay.budgetbuddy.data.local.SettingsDataStore
import com.nilay.budgetbuddy.data.local.TokenHolder
import com.nilay.budgetbuddy.data.remote.api.AuthService
import com.nilay.budgetbuddy.data.remote.dto.LoginRequest
import com.nilay.budgetbuddy.data.remote.dto.RegisterRequest
import com.nilay.budgetbuddy.data.remote.dto.UpdateCurrencyRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authService: AuthService,
    private val settingsDataStore: SettingsDataStore,
    private val tokenHolder: TokenHolder
) {
    val isLoggedIn: Flow<Boolean> = settingsDataStore.tokenFlow.map { it != null }
    val userNameFlow: Flow<String?> = settingsDataStore.userNameFlow
    val userEmailFlow: Flow<String?> = settingsDataStore.userEmailFlow

    suspend fun register(fullName: String, email: String, password: String) {
        val response = authService.register(RegisterRequest(fullName, email, password))
        persistSession(response.token)
    }

    suspend fun login(email: String, password: String) {
        val response = authService.login(LoginRequest(email, password))
        persistSession(response.token)
    }

    suspend fun logout() {
        tokenHolder.token = null
        settingsDataStore.clearSession()
    }

    suspend fun updateCurrency(currency: String) {
        authService.updateCurrency(UpdateCurrencyRequest(currency))
        settingsDataStore.updateCurrency(currency)
    }

    private suspend fun persistSession(token: String) {
        tokenHolder.token = token
        val me = authService.getMe()
        settingsDataStore.saveSession(token, me.fullName, me.email, me.currency)
    }
}
