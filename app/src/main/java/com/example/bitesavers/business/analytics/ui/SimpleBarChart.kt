package com.example.bitesavers.business.analytics.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitesavers.business.analytics.data.DailyMetricPoint
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

/**
 * A plain hand-drawn bar chart — deliberately not using a charting library
 * (MPAndroidChart, Vico, etc.) to avoid adding a new Gradle dependency and
 * the version-compatibility risk that comes with it, for a chart this
 * simple.
 */
@Composable
fun SimpleBarChart(
    points: List<DailyMetricPoint>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    chartHeight: androidx.compose.ui.unit.Dp = 160.dp
) {
    val niceMax = niceCeiling(points.maxOfOrNull { it.value } ?: 0.0)
    val steps = 4

    Row(modifier = modifier) {
        // Y-axis labels
        Column(
            modifier = Modifier
                .width(32.dp)
                .height(chartHeight),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            for (i in steps downTo 0) {
                Text(
                    text = (niceMax / steps * i).toInt().toString(),
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.width(6.dp))
        Column(modifier = Modifier.weight(1f)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight)
            ) {
                // gridlines
                val stepPx = size.height / steps
                for (i in 0..steps) {
                    val y = size.height - i * stepPx
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.35f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.5f
                    )
                }
                if (points.isEmpty()) return@Canvas
                val spacing = size.width * 0.05f
                val barWidth = (size.width - spacing * (points.size + 1)) / points.size
                points.forEachIndexed { index, point ->
                    val ratio = if (niceMax > 0) (point.value / niceMax).toFloat() else 0f
                    val barHeight = size.height * ratio
                    val left = spacing + index * (barWidth + spacing)
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(left, size.height - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                points.forEach { point ->
                    Text(
                        point.label,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/** Rounds up to a "nice" axis maximum (e.g. 187 -> 200, 43 -> 50), like most charting libraries do. */
private fun niceCeiling(value: Double): Double {
    if (value <= 0) return 10.0
    val magnitude = 10.0.pow(floor(log10(value)))
    val normalized = value / magnitude
    val niceNormalized = when {
        normalized <= 1 -> 1.0
        normalized <= 2 -> 2.0
        normalized <= 5 -> 5.0
        else -> 10.0
    }
    return niceNormalized * magnitude
}
