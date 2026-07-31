package com.nilay.budgetbuddy.data.remote.api

import com.nilay.budgetbuddy.data.remote.dto.CategoryDto
import retrofit2.http.*

interface CategoryService {
    @GET("categories")
    suspend fun getCategories(): List<CategoryDto>

    @POST("categories")
    suspend fun createCategory(@Body category: CategoryDto): CategoryDto

    @PUT("categories/{id}")
    suspend fun updateCategory(@Path("id") id: Long, @Body category: CategoryDto): CategoryDto

    @DELETE("categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Long)
}
