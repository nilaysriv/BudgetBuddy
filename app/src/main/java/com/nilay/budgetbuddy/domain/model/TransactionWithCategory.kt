package com.nilay.budgetbuddy.domain.model

import androidx.room.Embedded
import androidx.room.Relation
import com.nilay.budgetbuddy.data.local.entity.Category
import com.nilay.budgetbuddy.data.local.entity.Transaction

data class TransactionWithCategory(
    @Embedded val transaction: Transaction,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: Category
)
