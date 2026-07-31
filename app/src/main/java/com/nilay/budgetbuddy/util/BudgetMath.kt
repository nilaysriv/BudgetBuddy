package com.nilay.budgetbuddy.util

import com.nilay.budgetbuddy.domain.model.TransactionType
import com.nilay.budgetbuddy.domain.model.TransactionWithCategory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val monthKeyFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

fun monthKeyOf(epochMillis: Long): String = monthKeyFormat.format(Date(epochMillis))

/** Total expense spend for a category within a single "yyyy-MM" month — used for budget-vs-actual comparisons. */
fun List<TransactionWithCategory>.spentForCategoryInMonth(categoryId: Long, monthYear: String): Double =
    filter {
        it.transaction.categoryId == categoryId &&
            it.transaction.type == TransactionType.EXPENSE &&
            monthKeyOf(it.transaction.date) == monthYear
    }.sumOf { it.transaction.amount }
