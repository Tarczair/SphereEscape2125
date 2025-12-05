package com.example.sphereescape2125.screens.obstacle

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.PI
import kotlin.random.Random
import kotlin.math.cos
import kotlin.math.sin

data class WallObstacle(
    val startRing: RingObstacle,
    val endRing: RingObstacle,
    val startRadius: Float,
    val endRadius: Float,
    val angle: Float,
    val color: Color
)

fun anglesFarEnoughPx(
    newAngle: Float,
    used: List<Float>,
    minPx: Float,
    radius: Float
): Boolean {
    val minDeg = (minPx / radius) * (180f / PI.toFloat())

    return used.all { existing ->
        val diff = kotlin.math.abs(newAngle - existing)
        val wrap = 360f - diff
        val smallest = kotlin.math.min(diff, wrap)
        smallest >= minDeg
    }
}


fun adjustAngleOutsideGapsSector(
    angle: Float,
    current: RingObstacle,
    next: RingObstacle,
    minAngle: Float,
    maxAngle: Float
): Float {
    var adjusted = angle

    val allGaps = current.gaps.map { gap ->
        gap - 5f to gap + (gapSize / current.innerRadius) * (180f / PI.toFloat()) + 5f
    } + next.gaps.map { gap ->
        gap - 5f to gap + (gapSize / next.innerRadius) * (180f / PI.toFloat()) + 5f
    }

    for ((start, end) in allGaps) {
        if (adjusted in start..end) {
            adjusted = end + 1f
        }
    }

    // pilnujemy, żeby kąt nie wyszedł poza swój sektor
    if (adjusted > maxAngle) adjusted = maxAngle - 1f
    if (adjusted < minAngle) adjusted = minAngle + 1f

    return ((adjusted * 10).toInt() / 10f)
}


fun generateWallsBetweenRings(
    rings: List<RingObstacle>,
    wallsPerGap: Int = 0,
    color: Color = Color.Red
): List<WallObstacle> {

    val walls = mutableListOf<WallObstacle>()

    val sectorSize = 360f / wallsPerGap

    val allAngles = generateSequence(0f) { it + 0.1f }
        .takeWhile { it < 360f }
        .toList()

    for (i in 0 until rings.lastIndex) {
        val usedAngles = mutableListOf<Float>()

        val current = rings[i]
        val next = rings[i + 1]

        if (current.wallsGenerated) continue

        val validAngles = allAngles.filter { angle ->
            val inCurrent = current.gaps.any { gap ->
                val gapStart = gap - 5f
                val gapEnd = gap + (gapSize / current.innerRadius) * (180f / PI.toFloat()) + 5f
                angle in gapStart..gapEnd
            }

            val inNext = next.gaps.any { gap ->
                val gapStart = gap - 5f
                val gapEnd = gap + (gapSize / next.innerRadius) * (180f / PI.toFloat()) + 5f
                angle in gapStart..gapEnd
            }

            (!inCurrent && !inNext)
        }

        if (validAngles.isEmpty()) continue
        current.wallsGenerated = true

        repeat(wallsPerGap) { index ->

            val minAngle = (sectorSize * index)
            val maxAngle = minAngle + sectorSize

            val filteredAngles = validAngles.filter { it in minAngle..maxAngle }
            if (filteredAngles.isEmpty()) return@repeat

            var baseAngle = filteredAngles.random()
            baseAngle = (baseAngle * 10).toInt() / 10f

            val adjustedAngle = adjustAngleOutsideGapsSector(baseAngle, current, next, minAngle, maxAngle)

            if (filteredAngles.isNotEmpty()) {

                val type = Random.nextInt(4)

                val (startR, endR) = when (type) {
                    0, 1 -> current.outerRadius - 5f to next.innerRadius + 5f
                    2 -> current.outerRadius to (current.outerRadius + (next.innerRadius - current.outerRadius) / 3f) + 10
                    else -> next.innerRadius + 5f to (current.outerRadius + (next.innerRadius - current.outerRadius) / 1.5f) - 10f
                }

                // TU DODAJEMY FILTROWANIE PO ODLEGŁOŚCI 100px
                val avgRadius = (startR + endR) / 2f
                if (!anglesFarEnoughPx(adjustedAngle, usedAngles, 200f, avgRadius)) {
                    return@repeat
                }

                usedAngles.add(adjustedAngle)

                walls.add(
                    WallObstacle(
                        startRing = current,
                        endRing = next,
                        startRadius = startR,
                        endRadius = endR,
                        angle = adjustedAngle % 360f,
                        color = color
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