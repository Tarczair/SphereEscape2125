package com.example.sphereescape2125.screens.obstacle


import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.hypot
import kotlin.math.cos
import kotlin.math.sin

fun isCircleCollidingWithRing(
    circleCenter: Offset,
    circleRadius: Float,
    ring: RingObstacle,
): Pair<Boolean, Boolean> {

    val distance = hypot(
        circleCenter.x - ring.center.x,
        circleCenter.y - ring.center.y
    )
    val gapSize = 120f

    // kula zbyt daleko lub zbyt blisko (poza pierścieniem)
    if (distance + circleRadius < ring.innerRadius + 20 || distance - circleRadius > ring.outerRadius + 20) {
        return false to false
    }

    // obliczamy kąt położenia kuli
    val angle = Math.toDegrees(
        kotlin.math.atan2(
            (circleCenter.y - ring.center.y).toDouble(),
            (circleCenter.x - ring.center.x).toDouble()
        )
    ).let { if (it < 0) it + 360 else it }

    // sprawdzamy, czy kula jest w jednej z dziur (gapów)
    for (gap in ring.gaps) {
        val gapStart = gap
        val gapEnd = gapStart + ((gapSize / ring.innerRadius) * (180f / PI.toFloat()) % 360f)

        // obsługa przypadku, gdy dziura przekracza 360°
        val inGap = if (gapEnd > 360f) {
            angle >= gapStart || angle <= (gapEnd - 360f)
        } else {
            angle in gapStart..gapEnd
        }

        val startRad = Math.toRadians(gapStart.toDouble())
        val startX = ring.center.x + ring.outerRadius * cos(startRad)
        val startY = ring.center.y + ring.outerRadius * sin(startRad)
        val distStart = hypot(circleCenter.x - startX, circleCenter.y - startY)

        val endRad = Math.toRadians(gapEnd.toDouble())
        val endX = ring.center.x + ring.outerRadius * cos(endRad)
        val endY = ring.center.y + ring.outerRadius * sin(endRad)
        val distEnd = hypot(circleCenter.x - endX, circleCenter.y - endY)

        if (distance + circleRadius > ring.innerRadius + 20 && distance - circleRadius < ring.outerRadius + 20) {
            if (distEnd <= circleRadius || distStart <= circleRadius) {
                return true to false
            }
        }

        if (inGap) {
            return false to true // kula jest w dziurze
        }
    }

    // jeśli nie w dziurze, to znaczy że dotyka pierścienia
    return true to false
}

fun getWallCollisionInfo(
    circleCenter: Offset,
    circleRadius: Float,
    wall: WallObstacle
): Triple<Offset, Float, Offset>? {
    val angleRad = Math.toRadians(wall.angle.toDouble())

    val start = Offset(
        x = wall.startRing.center.x + wall.startRadius * cos(angleRad).toFloat(),
        y = wall.startRing.center.y + wall.startRadius * sin(angleRad).toFloat()
    )
    val end = Offset(
        x = wall.endRing.center.x + wall.endRadius * cos(angleRad).toFloat(),
        y = wall.endRing.center.y + wall.endRadius * sin(angleRad).toFloat()
    )

    // Wektor kierunkowy ściany
    val dx = end.x - start.x
    val dy = end.y - start.y
    if (dx == 0f && dy == 0f) return null

    // Znajdź najbliższy punkt na linii
    val t = ((circleCenter.x - start.x) * dx + (circleCenter.y - start.y) * dy) / (dx * dx + dy * dy)
    val clampedT = t.coerceIn(0f, 1f)
    val closest = Offset(start.x + clampedT * dx, start.y + clampedT * dy)

    // Dystans do najbliższego punktu
    val distance = hypot(circleCenter.x - closest.x, circleCenter.y - closest.y)
    val wallHalfWidth = 25f
    val collisionThreshold = circleRadius + wallHalfWidth

    if (distance <= collisionThreshold) {
        // Obliczamy normalną z wektora kierunkowego ściany (dx, dy)
        // Wektor prostopadły to (-dy, dx)
        val normalLen = hypot(dx, dy)
        if (normalLen == 0f) return null

        var normalX = -dy / normalLen
        var normalY = dx / normalLen

        // Musimy się upewnić, że normalna jest skierowana na zewnątrz ściany,
        // Używamy wektora od środka ściany do kulki.
        val vecToCircleX = circleCenter.x - closest.x
        val vecToCircleY = circleCenter.y - closest.y

        val dot = (normalX * vecToCircleX) + (normalY * vecToCircleY)

        if (dot < 0) {
            normalX = -normalX
            normalY = -normalY
        }

        return Triple(closest, distance, Offset(normalX, normalY)) // Jest kolizja!
    }

    return null // Nie ma kolizji
}