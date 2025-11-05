package com.example.sphereescape2125.screens.obstacle

import android.util.Log
import androidx.compose.animation.core.repeatable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.floor
import kotlin.random.Random
import kotlin.math.cos
import kotlin.math.sin

data class WallObstacle(
    val startRing: RingObstacle,
    val endRing: RingObstacle,
    val startRadius: Float,
    val endRadius: Float,
    val angle: Float,
    val color: Color = Color.Gray,
)

fun generateWallsBetweenRings(
    rings: List<RingObstacle>,
    existingWalls: List<WallObstacle> = emptyList(),
    wallsPerGap: Int = 2 // liczba ścian między pierścieniami
): List<WallObstacle> {

    val walls = mutableListOf<WallObstacle>()

    for (i in 0 until rings.lastIndex) {
        val current = rings[i]
        val next = rings[i + 1]

        val validAngles = (0..359).filter { angle ->
            // sprawdź, czy kąt nie wpada w żaden gap
            current.gaps.none { gap ->
                val gapStart = gap - 5f
                val gapEnd = gap + (gapSize / current.innerRadius) * (180f / PI.toFloat()) + 5f
                angle.toFloat() in gapStart..gapEnd
            }
        }

        repeat(wallsPerGap) { index ->
            val minAngle = (360 / wallsPerGap) * index
            val maxAngle = minAngle + (360 / wallsPerGap)

            val alreadyHasWalls = existingWalls.any { it.startRing == current && it.endRing == next }
            if (alreadyHasWalls) return@repeat

            val filteredAngles = (validAngles).filter { angle -> angle in minAngle..maxAngle }
            if (filteredAngles.isNotEmpty()) {
                val angle = filteredAngles.random().toFloat()

                // losowy typ ściany: łącząca, pół, lub przesunięta
                val type = Random.nextInt(3)

                val (startR, endR) = when (type) {
                    0 -> current.outerRadius + 20 to next.innerRadius + 25 // pełna ściana
                    1 -> current.outerRadius + 20 to (current.outerRadius + (next.innerRadius - current.outerRadius) / 1.5f) - 30 // połowa
                    else -> next.innerRadius + 25  to (current.outerRadius + (next.innerRadius - current.outerRadius) / 1.5f) - 10 // połowa
                }

                walls.add(
                    WallObstacle(
                        startRing = current,
                        endRing = next,
                        startRadius = startR,
                        endRadius = endR,
                        angle = angle
                    )
                )
            }
        }
    }

    return walls
}


fun DrawScope.drawWalls(walls: List<WallObstacle>) {
    for (wall in walls) {
        val angleRad = Math.toRadians(wall.angle.toDouble())
        val start = Offset(
            x = wall.startRing.center.x + wall.startRadius * cos(angleRad).toFloat(),
            y = wall.startRing.center.y + wall.startRadius * sin(angleRad).toFloat()
        )
        val end = Offset(
            x = wall.endRing.center.x + wall.endRadius * cos(angleRad).toFloat(),
            y = wall.endRing.center.y + wall.endRadius * sin(angleRad).toFloat()
        )

        drawLine(
            color = wall.color,
            start = start,
            end = end,
            strokeWidth = 50f
        )
    }
}

