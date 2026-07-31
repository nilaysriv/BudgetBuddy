package com.nilay.budgetbuddy.data.local

import androidx.room.TypeConverter
import com.nilay.budgetbuddy.domain.model.TransactionType

class Converters {
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String {
        return type.name
    }

    @TypeConverter
    fun toTransactionType(name: String): TransactionType {
        return TransactionType.valueOf(name)
    }
}
