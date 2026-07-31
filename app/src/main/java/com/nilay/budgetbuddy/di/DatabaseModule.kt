package com.nilay.budgetbuddy.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nilay.budgetbuddy.data.local.BudgetBuddyDatabase
import com.nilay.budgetbuddy.data.local.dao.BudgetDao
import com.nilay.budgetbuddy.data.local.dao.CategoryDao
import com.nilay.budgetbuddy.data.local.dao.TransactionDao
import com.nilay.budgetbuddy.data.local.entity.Category
import com.nilay.budgetbuddy.domain.model.TransactionType
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): BudgetBuddyDatabase {
        var database: BudgetBuddyDatabase? = null
        val callback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    database?.categoryDao()?.let { seedDefaultCategories(it) }
                }
            }
        }
        database = Room.databaseBuilder(
            context,
            BudgetBuddyDatabase::class.java,
            BudgetBuddyDatabase.DATABASE_NAME
        ).addCallback(callback)
            // ponytail: no real users on this schema yet, destructive migration beats hand-written Migration objects
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
        return database
    }

    private suspend fun seedDefaultCategories(categoryDao: CategoryDao) {
        val defaultCategories = listOf(
            Category(name = "Food", type = TransactionType.EXPENSE, iconKey = "restaurant", colorHex = "#FF5722", isDefault = true),
            Category(name = "Transport", type = TransactionType.EXPENSE, iconKey = "directions_bus", colorHex = "#2196F3", isDefault = true),
            Category(name = "Rent", type = TransactionType.EXPENSE, iconKey = "home", colorHex = "#795548", isDefault = true),
            Category(name = "Utilities", type = TransactionType.EXPENSE, iconKey = "bolt", colorHex = "#FFEB3B", isDefault = true),
            Category(name = "Shopping", type = TransactionType.EXPENSE, iconKey = "shopping_cart", colorHex = "#E91E63", isDefault = true),
            Category(name = "Entertainment", type = TransactionType.EXPENSE, iconKey = "movie", colorHex = "#9C27B0", isDefault = true),
            Category(name = "Health", type = TransactionType.EXPENSE, iconKey = "medical_services", colorHex = "#4CAF50", isDefault = true),
            Category(name = "Salary", type = TransactionType.INCOME, iconKey = "payments", colorHex = "#00BCD4", isDefault = true),
            Category(name = "Other", type = TransactionType.EXPENSE, iconKey = "more_horiz", colorHex = "#607D8B", isDefault = true)
        )
        categoryDao.insertCategories(defaultCategories)
    }

    @Provides
    fun provideTransactionDao(db: BudgetBuddyDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideCategoryDao(db: BudgetBuddyDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideBudgetDao(db: BudgetBuddyDatabase): BudgetDao = db.budgetDao()
}
