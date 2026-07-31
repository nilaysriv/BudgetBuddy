package com.nilay.budgetbuddy.domain.model

/** A single point on an income-vs-expense bar chart — a day, a month, whatever the label represents. */
data class TrendPoint(
    val label: String,
    val income: Double,
    val expense: Double
)
