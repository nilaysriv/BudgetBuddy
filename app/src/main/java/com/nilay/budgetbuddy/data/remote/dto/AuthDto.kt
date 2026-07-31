package com.nilay.budgetbuddy.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(val fullName: String, val email: String, val password: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class AuthResponse(val token: String)

@Serializable
data class MeResponse(val id: Long, val email: String, val fullName: String, val currency: String, val createdAt: String)

@Serializable
data class UpdateCurrencyRequest(val currency: String)
