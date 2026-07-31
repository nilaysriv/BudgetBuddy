package com.nilay.budgetbuddy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nilay.budgetbuddy.data.local.dao.BudgetDao
import com.nilay.budgetbuddy.data.local.dao.CategoryDao
import com.nilay.budgetbuddy.data.local.dao.TransactionDao
import com.nilay.budgetbuddy.data.local.entity.Budget
import com.nilay.budgetbuddy.data.local.entity.Category
import com.nilay.budgetbuddy.data.local.entity.Transaction

@Database(
    entities = [Transaction::class, Category::class, Budget::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BudgetBuddyDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        const val DATABASE_NAME = "budget_buddy_db"
    }
}
