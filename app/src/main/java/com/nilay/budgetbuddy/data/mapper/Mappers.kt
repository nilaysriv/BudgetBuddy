package com.nilay.budgetbuddy.data.mapper

import com.nilay.budgetbuddy.data.local.entity.Budget as BudgetEntity
import com.nilay.budgetbuddy.data.local.entity.Category as CategoryEntity
import com.nilay.budgetbuddy.data.local.entity.Transaction as TransactionEntity
import com.nilay.budgetbuddy.data.remote.dto.BudgetDto
import com.nilay.budgetbuddy.data.remote.dto.CategoryDto
import com.nilay.budgetbuddy.data.remote.dto.TransactionDto
import com.nilay.budgetbuddy.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.*

private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

fun TransactionDto.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id ?: 0,
        amount = amount,
        type = try { TransactionType.valueOf(type.uppercase()) } catch (e: Exception) { TransactionType.EXPENSE },
        categoryId = categoryId,
        date = try { dateFormat.parse(date)?.time ?: 0L } catch (e: Exception) { 0L },
        note = note
    )
}

fun TransactionEntity.toDto(): TransactionDto {
    return TransactionDto(
        id = if (id == 0L) null else id,
        amount = amount,
        type = type.name,
        categoryId = categoryId,
        date = dateFormat.format(Date(date)),
        note = note
    )
}

fun CategoryDto.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id ?: 0,
        name = name,
        type = try { TransactionType.valueOf(type.uppercase()) } catch (e: Exception) { TransactionType.EXPENSE },
        iconKey = iconKey,
        colorHex = colorHex,
        isDefault = isDefault,
        parentCategoryId = parentCategoryId
    )
}

fun CategoryEntity.toDto(): CategoryDto {
    return CategoryDto(
        id = if (id == 0L) null else id,
        name = name,
        type = type.name,
        iconKey = iconKey,
        colorHex = colorHex,
        isDefault = isDefault,
        parentCategoryId = parentCategoryId
    )
}

fun BudgetDto.toEntity(): BudgetEntity {
    return BudgetEntity(
        id = id ?: 0,
        categoryId = categoryId,
        monthYear = monthYear,
        budgetAmount = budgetAmount
    )
}

fun BudgetEntity.toDto(): BudgetDto {
    return BudgetDto(
        id = if (id == 0L) null else id,
        categoryId = categoryId,
        monthYear = monthYear,
        budgetAmount = budgetAmount
    )
}
