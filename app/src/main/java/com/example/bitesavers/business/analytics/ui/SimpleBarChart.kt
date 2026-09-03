package com.example.bitesavers.business.analytics.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitesavers.business.analytics.data.DailyMetricPoint
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

@Composable
fun SimpleBarChart(
    points: List<DailyMetricPoint>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    chartHeight: Dp = 170.dp
) {
    val textMeasurer = rememberTextMeasurer()
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = Color.LightGray.copy(alpha = 0.35f)

    val maxVal = points.maxOfOrNull { it.value } ?: 0.0
    val (niceMax, steps) = calculateScale(maxVal)

    val labelStyle = TextStyle(
        fontSize = 10.sp,
        color = labelColor
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight)
    ) {
        val yAxisPaddingRight = 10.dp.toPx()
        val yAxisWidth = 28.dp.toPx()
        val bottomLabelHeight = 22.dp.toPx()
        val topPadding = 10.dp.toPx()

        val plotWidth = size.width - yAxisWidth - yAxisPaddingRight
        val plotHeight = size.height - bottomLabelHeight - topPadding
        val plotLeft = yAxisWidth + yAxisPaddingRight
        val plotBottom = topPadding + plotHeight

        // 1. Draw Y-Axis Labels & Horizontal Grid Lines perfectly aligned
        for (i in 0..steps) {
            val stepValue = (niceMax / steps) * i
            val formattedLabel = if (stepValue % 1.0 == 0.0) {
                stepValue.toInt().toString()
            } else {
                "%.1f".format(stepValue)
            }

            val y = plotBottom - (i.toFloat() / steps.toFloat()) * plotHeight

            // Measure label text
            val textLayout = textMeasurer.measure(
                text = formattedLabel,
                style = labelStyle
            )

            // Draw Y-axis text perfectly centered vertically against the grid line
            val textX = yAxisWidth - textLayout.size.width
            val textY = y - (textLayout.size.height / 2f)

            drawText(
                textMeasurer = textMeasurer,
                text = formattedLabel,
                topLeft = Offset(textX, textY),
                style = labelStyle
            )

            // Draw grid line extending right from the Y-axis
            drawLine(
                color = gridColor,
                start = Offset(plotLeft, y),
                end = Offset(size.width, y),
                strokeWidth = 1.5f
            )
        }

        // 2. Draw Bars and X-Axis Day Labels ("Mon", "Tue", etc.)
        if (points.isNotEmpty()) {
            val barCount = points.size
            val barSlotWidth = plotWidth / barCount
            val barWidth = barSlotWidth * 0.50f // 50% bar width, 50% spacing

            points.forEachIndexed { index, point ->
                val slotCenterX = plotLeft + (index * barSlotWidth) + (barSlotWidth / 2f)
                val barLeft = slotCenterX - (barWidth / 2f)

                val ratio = if (niceMax > 0) (point.value / niceMax).toFloat().coerceIn(0f, 1f) else 0f
                val barHeight = plotHeight * ratio

                // Draw Bar
                if (barHeight > 0f) {
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(barLeft, plotBottom - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }

                // Measure X-axis day label ("Mon", "Tue", etc.)
                val dayLayout = textMeasurer.measure(
                    text = point.label,
                    style = labelStyle
                )

                // Draw X-axis day label centered below the bar
                val dayX = slotCenterX - (dayLayout.size.width / 2f)
                val dayY = plotBottom + 6.dp.toPx()

                drawText(
                    textMeasurer = textMeasurer,
                    text = point.label,
                    topLeft = Offset(dayX, dayY),
                    style = labelStyle
                )
            }
        }
    }
}

/**
 * Calculates integer-friendly steps and maximum scales so grid lines
 * and numbers always increment linearly (e.g. 0, 1, 2, 3, 4, 5 or 0, 5, 10, 15, 20).
 */
private fun calculateScale(maxValue: Double): Pair<Double, Int> {
    val max = if (maxValue <= 0.0) 5.0 else maxValue
    return when {
        max <= 5.0 -> 5.0 to 5       // Steps: 0, 1, 2, 3, 4, 5 (Step size: 1)
        max <= 10.0 -> 10.0 to 5     // Steps: 0, 2, 4, 6, 8, 10 (Step size: 2)
        max <= 20.0 -> 20.0 to 4     // Steps: 0, 5, 10, 15, 20 (Step size: 5)
        max <= 40.0 -> 40.0 to 4     // Steps: 0, 10, 20, 30, 40 (Step size: 10)
        max <= 50.0 -> 50.0 to 5     // Steps: 0, 10, 20, 30, 40, 50 (Step size: 10)
        max <= 100.0 -> 100.0 to 4   // Steps: 0, 25, 50, 75, 100 (Step size: 25)
        max <= 200.0 -> 200.0 to 4   // Steps: 0, 50, 100, 150, 200 (Step size: 50)
        max <= 500.0 -> 500.0 to 5   // Steps: 0, 100, 200, 300, 400, 500 (Step size: 100)
        else -> {
            val rawStep = max / 4.0
            val magnitude = 10.0.pow(floor(log10(rawStep)))
            val normalized = rawStep / magnitude
            val niceStep = when {
                normalized <= 1.0 -> 1.0
                normalized <= 2.0 -> 2.0
                normalized <= 2.5 -> 2.5
                normalized <= 5.0 -> 5.0
                else -> 10.0
            } * magnitude
            (niceStep * 4) to 4
        }
    }
}