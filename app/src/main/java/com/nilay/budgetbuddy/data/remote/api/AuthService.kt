package com.nilay.budgetbuddy.data.remote.api

import com.nilay.budgetbuddy.data.remote.dto.AuthResponse
import com.nilay.budgetbuddy.data.remote.dto.LoginRequest
import com.nilay.budgetbuddy.data.remote.dto.MeResponse
import com.nilay.budgetbuddy.data.remote.dto.RegisterRequest
import com.nilay.budgetbuddy.data.remote.dto.UpdateCurrencyRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface AuthService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @GET("me")
    suspend fun getMe(): MeResponse

    @PATCH("me")
    suspend fun updateCurrency(@Body request: UpdateCurrencyRequest): MeResponse
}
