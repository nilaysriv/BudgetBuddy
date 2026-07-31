package com.nilay.budgetbuddy.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.nilay.budgetbuddy.ui.reports.CategoryTrendSeries

/** A compact, axis-free multi-series sparkline: one colored polyline per category, over the same set of months. */
@Composable
fun CategoryTrendChart(series: List<CategoryTrendSeries>, monthLabels: List<String>, modifier: Modifier = Modifier) {
    if (series.isEmpty() || monthLabels.size < 2) {
        Text(
            "Not enough data yet",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier
        )
        return
    }

    val maxValue = (series.flatMap { it.monthlyAmounts }.maxOrNull() ?: 0.0).coerceAtLeast(1.0)

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val pointCount = monthLabels.size
            val stepX = size.width / (pointCount - 1)

            series.forEach { s ->
                val color = colorFor(s.colorHex)
                val path = Path()
                s.monthlyAmounts.forEachIndexed { index, amount ->
                    val x = stepX * index
                    val y = size.height - (amount / maxValue).toFloat() * size.height
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                s.monthlyAmounts.forEachIndexed { index, amount ->
                    val x = stepX * index
                    val y = size.height - (amount / maxValue).toFloat() * size.height
                    drawCircle(color = color, radius = 5f, center = Offset(x, y))
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            monthLabels.forEach { label ->
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Column {
            series.forEach { s ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(colorFor(s.colorHex), CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(s.categoryName, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

private fun colorFor(colorHex: String): Color = try {
    Color(android.graphics.Color.parseColor(colorHex))
} catch (e: Exception) {
    Color.Gray
}
