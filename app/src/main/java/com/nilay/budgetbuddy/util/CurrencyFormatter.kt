package com.nilay.budgetbuddy.util

import androidx.compose.runtime.compositionLocalOf
import java.text.NumberFormat
import java.util.Locale

val LocalCurrency = compositionLocalOf { "INR" }

object CurrencyFormatter {
    private val localeByCurrency = mapOf(
        "INR" to Locale("en", "IN"),
        "USD" to Locale.US,
        "EUR" to Locale.GERMANY,
        "GBP" to Locale.UK
    )

    val supportedCurrencies: List<String> = listOf("INR", "USD", "EUR", "GBP")

    fun format(amount: Double, currency: String): String {
        val locale = localeByCurrency[currency] ?: localeByCurrency.getValue("INR")
        val format = NumberFormat.getCurrencyInstance(locale)
        format.currency = java.util.Currency.getInstance(currency)
        return format.format(amount)
    }
}
