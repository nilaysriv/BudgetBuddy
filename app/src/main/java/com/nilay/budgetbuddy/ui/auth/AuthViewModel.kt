package com.nilay.budgetbuddy.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nilay.budgetbuddy.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.ResponseBody
import retrofit2.HttpException
import javax.inject.Inject

enum class AuthMode { LOGIN, REGISTER }

data class AuthUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun toggleMode() {
        _uiState.update {
            it.copy(
                mode = if (it.mode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN,
                error = null
            )
        }
    }

    fun onFullNameChange(value: String) = _uiState.update { it.copy(fullName = value, error = null) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, error = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, error = null) }

    fun submit() {
        val state = _uiState.value
        if (state.isLoading) return

        val email = state.email.trim()
        val fullName = state.fullName.trim()
        if (email.isBlank() || state.password.isBlank() || (state.mode == AuthMode.REGISTER && fullName.isBlank())) {
            _uiState.update { it.copy(error = "Please fill in all fields") }
            return
        }
        if (state.password.length < 8) {
            _uiState.update { it.copy(error = "Password must be at least 8 characters") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                if (state.mode == AuthMode.LOGIN) {
                    authRepository.login(email, state.password)
                } else {
                    authRepository.register(fullName, email, state.password)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.toAuthErrorMessage()) }
                return@launch
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}

private fun Exception.toAuthErrorMessage(): String {
    if (this is HttpException) {
        val serverMessage = response()?.errorBody()?.extractErrorField()
        if (serverMessage != null) return serverMessage
        return if (code() == 401) "Invalid email or password" else "Something went wrong (code ${code()})"
    }
    return "Couldn't reach the server. Check your connection and try again."
}

private fun ResponseBody.extractErrorField(): String? = try {
    Json.parseToJsonElement(string()).let { (it as? JsonObject)?.get("error")?.jsonPrimitive?.content }
} catch (e: Exception) {
    null
}
