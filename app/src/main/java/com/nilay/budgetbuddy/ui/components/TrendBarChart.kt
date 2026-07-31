package com.nilay.budgetbuddy.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nilay.budgetbuddy.domain.model.TrendPoint
import com.nilay.budgetbuddy.ui.theme.ExpenseColor
import com.nilay.budgetbuddy.ui.theme.IncomeColor

/** Grouped income/expense bars, one pair per [points] entry — used for both the Dashboard's 7-day trend and Reports' month comparison. */
@Composable
fun TrendBarChart(points: List<TrendPoint>, modifier: Modifier = Modifier, barMaxHeight: Dp = 96.dp) {
    val maxValue = (points.maxOfOrNull { maxOf(it.income, it.expense) } ?: 0.0).coerceAtLeast(1.0)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        points.forEach { point ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.height(barMaxHeight)
                ) {
                    val incomeFraction = (point.income / maxValue).toFloat().coerceIn(0f, 1f)
                    val expenseFraction = (point.expense / maxValue).toFloat().coerceIn(0f, 1f)
                    val incomeHeight by animateDpAsState(targetValue = barMaxHeight * incomeFraction, label = "income")
                    val expenseHeight by animateDpAsState(targetValue = barMaxHeight * expenseFraction, label = "expense")
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height(incomeHeight.coerceAtLeast(3.dp))
                            .background(IncomeColor, RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height(expenseHeight.coerceAtLeast(3.dp))
                            .background(ExpenseColor, RoundedCornerShape(4.dp))
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(point.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
