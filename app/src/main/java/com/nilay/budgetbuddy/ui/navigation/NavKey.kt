package com.nilay.budgetbuddy.ui.navigation

import kotlinx.serialization.Serializable
import androidx.navigation3.runtime.NavKey

sealed interface BudgetBuddyRoute : NavKey {
    @Serializable
    data object Dashboard : BudgetBuddyRoute

    @Serializable
    data object Transactions : BudgetBuddyRoute

    @Serializable
    data class TransactionAddEdit(val transactionId: Long? = null) : BudgetBuddyRoute

    @Serializable
    data object Budgets : BudgetBuddyRoute

    @Serializable
    data class BudgetAddEdit(val budgetId: Long? = null) : BudgetBuddyRoute

    @Serializable
    data object Reports : BudgetBuddyRoute

    @Serializable
    data object Settings : BudgetBuddyRoute

    @Serializable
    data object Categories : BudgetBuddyRoute

    @Serializable
    data class CategoryAddEdit(val categoryId: Long? = null) : BudgetBuddyRoute
}
