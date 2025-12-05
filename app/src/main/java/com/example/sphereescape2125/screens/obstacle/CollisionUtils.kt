package com.example.sphereescape2125.screens.obstacle

import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.hypot
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.abs
import com.example.sphereescape2125.screens.obstacle.WallObstacle // Import WallObstacle

// Ten parametr musi być taki sam jak w Obstacle.kt
const val gapSizeCollision = 120f

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

    // Sprawdzenie fizycznej kolizji z pierścieniem (z uwzględnieniem promienia kulki)
    val collisionStart = ring.innerRadius - circleRadius
    val collisionEnd = ring.outerRadius + circleRadius

    if (distance < collisionStart || distance > collisionEnd) {
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
        val gapAngle = (gapSize / ring.innerRadius) * (180f / PI.toFloat())
        val gapStart = gap
        var gapEnd = (gapStart + gapAngle)
        if (gapEnd > 360f) gapEnd -= 360f

        // Sprawdzenie, czy kąt jest w luce (uwzględniające zawijanie 360 stopni)
        val inGap = if (gapEnd < gapStart) {
            angle >= gapStart || angle <= gapEnd
        } else {
            angle in gapStart..gapEnd
        }

        if (inGap) {
            return false to true // kula jest w dziurze
        }
    }

    // jeśli nie w dziurze, to znaczy że fizycznie dotyka pierścienia
    return true to false
}

fun getLineSegmentCollision(
    circleCenter: Offset,
    circleRadius: Float,
    start: Offset,
    end: Offset,
    normal: Offset
): Triple<Offset, Float, Offset>? {
    val dx = end.x - start.x
    val dy = end.y - start.y
    val lenSq = dx*dx + dy*dy
    if (lenSq == 0f) return null

    val t = ((circleCenter.x - start.x) * dx + (circleCenter.y - start.y) * dy) / lenSq
    val clampedT = t.coerceIn(0f, 1f)
    val closest = Offset(start.x + clampedT * dx, start.y + clampedT * dy)
    val dist = hypot(circleCenter.x - closest.x, circleCenter.y - closest.y)

    if (dist <= circleRadius) {
        return Triple(closest, dist, normal)
    }
    return null
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

    val dx = end.x - start.x
    val dy = end.y - start.y
    if (dx == 0f && dy == 0f) return null

    val t = ((circleCenter.x - start.x) * dx + (circleCenter.y - start.y) * dy) / (dx * dx + dy * dy)
    val clampedT = t.coerceIn(0f, 1f)
    val closest = Offset(start.x + clampedT * dx, start.y + clampedT * dy)

    val distance = hypot(circleCenter.x - closest.x, circleCenter.y - closest.y)
    val wallHalfWidth = 25f
    val collisionThreshold = circleRadius + wallHalfWidth

    if (distance <= collisionThreshold) {
        val normalLen = hypot(dx, dy)
        if (normalLen == 0f) return null

        var normalX = -dy / normalLen
        var normalY = dx / normalLen

        val vecToCircleX = circleCenter.x - closest.x
        val vecToCircleY = circleCenter.y - closest.y

        val dot = (normalX * vecToCircleX) + (normalY * vecToCircleY)

        if (dot < 0) {
            normalX = -normalX
            normalY = -normalY
        }

        return Triple(closest, distance, Offset(normalX, normalY))
    }

    return null
}