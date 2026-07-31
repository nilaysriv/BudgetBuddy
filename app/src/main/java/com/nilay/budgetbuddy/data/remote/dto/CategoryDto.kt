package com.nilay.budgetbuddy.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: Long? = null,
    val name: String,
    val type: String,
    val iconKey: String,
    val colorHex: String,
    val isDefault: Boolean = false,
    val parentCategoryId: Long? = null
)
