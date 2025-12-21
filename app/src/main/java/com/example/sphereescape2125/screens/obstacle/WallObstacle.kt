package com.example.sphereescape2125.screens

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.sphereescape2125.screens.obstacle.RingObstacle
import com.example.sphereescape2125.screens.obstacle.gapSize // Importujemy gapSize z Obstacle.kt
import kotlin.math.*
import kotlin.random.Random

// Używamy tej samej stałej co w Obstacle.kt lub definiujemy lokalnie dla pewności
const val COLLISION_GAP_SIZE = 120f

data class WallObstacle(
    val startRing: RingObstacle,
    val endRing: RingObstacle,
    val startRadius: Float,
    val endRadius: Float,
    val angle: Float,
    val color: Color
)

// Sprawdza odległość kątową od innych ścian
fun anglesFarEnoughPx(newAngle: Float, used: List<Float>, minPx: Float, radius: Float): Boolean {
    val minDeg = (minPx / radius) * (180f / PI.toFloat())
    return used.all { existing ->
        val diff = abs(newAngle - existing)
        val wrap = 360f - diff
        val smallest = min(diff, wrap)
        smallest >= minDeg
    }
}

// NOWA FUNKCJA: Sprawdza czy kąt nie wchodzi w dziurę (gap) pierścienia
fun isAngleBlockedByGaps(angle: Float, ring: RingObstacle): Boolean {
    // Margines bezpieczeństwa (np. 10 stopni), żeby ściana nie stykała się z krawędzią dziury
    val safetyMarginDeg = 8f
    val gapAngleWidth = (COLLISION_GAP_SIZE / ring.innerRadius) * (180f / PI.toFloat())

    for (gapStart in ring.gaps) {
        // Obliczamy początek i koniec dziury
        var gStart = gapStart - safetyMarginDeg
        var gEnd = gapStart + gapAngleWidth + safetyMarginDeg

        // Normalizacja kąta do 0-360 sprawdzania
        // Najprościej: sprawdzić czy angle wpada w zakres, uwzględniając "przejście przez zero"

        val angleNorm = if (angle < 0) angle + 360f else angle % 360f

        // Obsługa zawijania zakresu (np. gap od 350 do 10)
        val inGap = if (gEnd > 360f) {
            angleNorm >= gStart || angleNorm <= (gEnd - 360f)
        } else if (gStart < 0f) {
            angleNorm >= (gStart + 360f) || angleNorm <= gEnd
        } else {
            angleNorm in gStart..gEnd
        }

        if (inGap) return true
    }
    return false
}

fun generateWallsBetweenRings(rings: List<RingObstacle>, wallsPerGap: Int, color: Color): List<WallObstacle> {
    val walls = mutableListOf<WallObstacle>()
    if (rings.size < 2) return walls

    for (i in 0 until rings.size - 1) {
        val current = rings[i]
        val next = rings[i + 1]
        val sectorSize = 360f / wallsPerGap
        val usedAngles = mutableListOf<Float>()

        repeat(wallsPerGap) { index ->
            val minAngle = (sectorSize * index)
            val maxAngle = minAngle + sectorSize

            // Próbujemy wylosować poprawną ścianę 10 razy
            var attempt = 0
            var added = false

            while(attempt < 15 && !added) {
                val baseAngle = Random.nextFloat() * (maxAngle - minAngle) + minAngle

                // SPRAWDZENIE 1: Czy nie koliduje z innymi ścianami
                // SPRAWDZENIE 2: Czy nie zasłania przejścia w obecnym pierścieniu
                // SPRAWDZENIE 3: Czy nie zasłania przejścia w następnym pierścieniu
                val avgR = (current.outerRadius + next.innerRadius) / 2f

                if (anglesFarEnoughPx(baseAngle, usedAngles, 150f, avgR) &&
                    !isAngleBlockedByGaps(baseAngle, current) &&
                    !isAngleBlockedByGaps(baseAngle, next)) {

                    val type = Random.nextInt(4)
                    val (startR, endR) = when (type) {
                        0, 1 -> current.outerRadius - 5f to next.innerRadius + 5f
                        2 -> current.outerRadius to (current.outerRadius + (next.innerRadius - current.outerRadius) / 3f) + 10
                        else -> next.innerRadius + 5f to (current.outerRadius + (next.innerRadius - current.outerRadius) / 1.5f) - 10f
                    }

                    walls.add(WallObstacle(current, next, startR, endR, baseAngle % 360f, color))
                    usedAngles.add(baseAngle)
                    added = true
                }
                attempt++
            }
        }
    }
    return walls
}

// FUNKCJA DLA WSTRZĄSU (POPRAWIONA)
fun regenerateWallsForSpecificRing(
    ringIndex: Int,
    rings: List<RingObstacle>,
    walls: MutableList<WallObstacle>, // Upewnij się, że to SnapshotStateList z GameScreen
    wallsPerGap: Int,
    color: Color,
    playerAngle: Float
) {
    if (ringIndex < 0 || ringIndex >= rings.size - 1) return

    val current = rings[ringIndex]
    val next = rings[ringIndex + 1]

    // USUNIĘCIE: Czyścimy ściany powiązane z tymi konkretnymi pierścieniami
    // Używamy iteratora, aby bezpiecznie modyfikować listę podczas pętli
    val iterator = walls.iterator()
    while (iterator.hasNext()) {
        val w = iterator.next()
        if (w.startRing == current || w.endRing == next) {
            iterator.remove()
        }
    }

    // GENEROWANIE NOWYCH:
    val sectorSize = 360f / wallsPerGap
    val usedAngles = mutableListOf<Float>()
    val safeMargin = 40f

    repeat(wallsPerGap) { index ->
        val minA = (sectorSize * index)
        val maxA = minA + sectorSize
        var attempt = 0
        var found = false

        while (attempt < 20 && !found) {
            val angle = Random.nextFloat() * (maxA - minA) + minA
            val diff = abs(angle - playerAngle)
            val smallestDist = min(diff, 360f - diff)

            val blockedByGaps = isAngleBlockedByGaps(angle, current) || isAngleBlockedByGaps(angle, next)

            if (smallestDist > safeMargin && !blockedByGaps &&
                anglesFarEnoughPx(angle, usedAngles, 140f, current.outerRadius)) {

                walls.add(WallObstacle(
                    current, next,
                    current.outerRadius - 5f,
                    next.innerRadius + 5f,
                    angle % 360f,
                    color
                ))
                usedAngles.add(angle)
                found = true
            }
            attempt++
        }
    }
}

fun DrawScope.drawWalls(walls: List<WallObstacle>) {
    val visualOffset = -8f
    for (wall in walls) {
        val angleRad = Math.toRadians(wall.angle.toDouble())
        val start = Offset(
            x = wall.startRing.center.x + (wall.startRadius - visualOffset) * cos(angleRad).toFloat(),
            y = wall.startRing.center.y + (wall.startRadius - visualOffset) * sin(angleRad).toFloat()
        )
        val end = Offset(
            x = wall.endRing.center.x + (wall.endRadius - visualOffset) * cos(angleRad).toFloat(),
            y = wall.endRing.center.y + (wall.endRadius - visualOffset) * sin(angleRad).toFloat()
        )
        drawLine(color = wall.color, start = start, end = end, strokeWidth = 50f)
    }
}