package com.example.sphereescape2125.screens.obstacle

import androidx.compose.ui.geometry.Offset
import kotlin.math.hypot
import kotlin.math.cos
import kotlin.math.sin

/**
 * Sprawdza, czy kulka koliduje z pierścieniem i czy znajduje się w dziurze.
 * Zwraca:
 *  - Pair(true, false)  → kolizja z pierścieniem
 *  - Pair(false, true)  → kula w dziurze
 *  - Pair(false, false) → brak kontaktu
 */
fun isCircleCollidingWithRing(
    circleCenter: Offset,
    circleRadius: Float,
    ring: RingObstacle,
    isTriggered: Boolean
): Pair<Boolean, Boolean> {

    val distance = hypot(
        circleCenter.x - ring.center.x,
        circleCenter.y - ring.center.y
    )

    // kula zbyt daleko lub zbyt blisko (poza pierścieniem)
    if (distance + circleRadius < ring.innerRadius || distance - circleRadius > ring.outerRadius) {
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
        val gapStart = gap.first
        val gapEnd = gap.first + gap.second

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
