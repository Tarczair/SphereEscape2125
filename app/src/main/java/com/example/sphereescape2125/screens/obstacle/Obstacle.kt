package com.example.sphereescape2125.screens.obstacle

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

data class RingObstacle(
    val center: Offset,
    val outerRadius: Float,
    val innerRadius: Float,
    val color: Color = Color.Red,
    val gaps: List<Pair<Float, Float>> = emptyList()
)

fun DrawScope.drawRingWithGaps(obstacle: RingObstacle) {
    val totalAngles = mutableListOf<Pair<Float, Float>>()

    // Tworzymy segmenty kolidujące (pełny 360° minus dziury)
    var start = 0f
    for (gap in obstacle.gaps.sortedBy { it.first }) {
        if (start < gap.first) {
            totalAngles.add(start to (gap.first - start))
        }
        start = gap.first + gap.second
    }
    if (start < 360f) {
        totalAngles.add(start to (360f - start))
    }

    // Rysowanie każdego segmentu pierścienia
    for ((startAngle, sweepAngle) in totalAngles) {
        drawArc(
            color = obstacle.color,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(obstacle.center.x - obstacle.outerRadius, obstacle.center.y - obstacle.outerRadius),
            size = androidx.compose.ui.geometry.Size(obstacle.outerRadius * 2, obstacle.outerRadius * 2),
            style = Stroke(width = obstacle.outerRadius - obstacle.innerRadius)
        )
    }
}
