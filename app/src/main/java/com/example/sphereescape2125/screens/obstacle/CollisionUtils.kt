package com.example.sphereescape2125.screens.obstacle

import androidx.compose.ui.geometry.Offset
import com.example.sphereescape2125.screens.WallObstacle
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Stała określająca szerokość luki kolizyjnej.
 * Musi być synchronizowana z wartością `gapSize` w pliku [Obstacle.kt].
 */
const val gapSizeCollision = 120f

/**
 * Wykrywa kolizję kulistego obiektu gracza z pierścieniem (RingObstacle).
 *
 * Funkcja transformuje pozycję gracza do układu biegunowego (odległość i kąt od środka pierścienia),
 * aby łatwo sprawdzić, czy gracz znajduje się w "ciele" pierścienia, czy trafił w lukę (gap).
 *
 * @param circleCenter Pozycja środka kuli gracza.
 * @param circleRadius Promień kuli gracza.
 * @param ring Obiekt przeszkody pierścieniowej.
 * @return Para wartości logicznych (Pair<Boolean, Boolean>):
 * - `first` (isColliding): True, jeśli nastąpiła fizyczna kolizja z przeszkodą.
 * - `second` (isInsideGap): True, jeśli gracz bezpiecznie przelatuje przez lukę (trigger przejścia poziomu).
 */
fun isCircleCollidingWithRing(
    circleCenter: Offset,
    circleRadius: Float,
    ring: RingObstacle
): Pair<Boolean, Boolean> {

    val distance = hypot(circleCenter.x - ring.center.x, circleCenter.y - ring.center.y)

    val collisionStart = ring.innerRadius - circleRadius
    val collisionEnd = ring.outerRadius + circleRadius

    // 1. Sprawdzenie proste: Czy jesteśmy w ogóle w zasięgu promienia pierścienia?
    if (distance < collisionStart || distance > collisionEnd) {
        return false to false
    }

    // 2. Obliczenie kąta położenia gracza (0-360 stopni)
    val angle = Math.toDegrees(
        atan2(
            (circleCenter.y - ring.center.y).toDouble(),
            (circleCenter.x - ring.center.x).toDouble()
        )
    ).let { if (it < 0) it + 360 else it }

    // 3. Sprawdzenie, czy kąt pokrywa się z którąkolwiek z luk
    for (gap in ring.gaps) {
        val gapAngle = (gapSizeCollision / ring.innerRadius) * (180f / PI.toFloat())
        val gapStart = gap
        var gapEnd = (gapStart + gapAngle)
        if (gapEnd > 360f) gapEnd -= 360f

        // Logika sprawdzania zakresu kątowego z uwzględnieniem "zawijania" przy 360 stopniach
        val inGap = if (gapEnd < gapStart) {
            angle >= gapStart || angle <= gapEnd
        } else {
            angle in gapStart..gapEnd
        }

        if (inGap) {
            // Trafiliśmy w dziurę -> Brak kolizji fizycznej, flaga "insideGap" na true
            return false to true
        }
    }

    // Jeśli jesteśmy w promieniu pierścienia, ale nie w dziurze -> Kolizja
    return true to false
}

/**
 * Oblicza kolizję koła z dowolnym odcinkiem linii.
 *
 * Wykorzystuje rzutowanie wektora, aby znaleźć punkt na odcinku najbliższy środkowi koła.
 * Służy do obsługi kolizji ze ściankami bocznymi luk ([GapWall]).
 *
 * @param circleCenter Środek koła.
 * @param circleRadius Promień koła.
 * @param start Punkt początkowy odcinka.
 * @param end Punkt końcowy odcinka.
 * @param normal Znormalizowany wektor odbicia (predefiniowany dla ścianek luk).
 * @return Obiekt [Triple] zawierający:
 * - Punkt kolizji (najbliższy punkt na linii).
 * - Dystans do tego punktu.
 * - Wektor normalny odbicia.
 * Zwraca `null`, jeśli kolizja nie występuje.
 */
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

    // Obliczenie parametru t (rzutowanie punktu na prostą)
    val t = ((circleCenter.x - start.x) * dx + (circleCenter.y - start.y) * dy) / lenSq

    // Ograniczenie t do przedziału [0, 1], aby pozostać w granicach odcinka
    val clampedT = t.coerceIn(0f, 1f)

    val closest = Offset(start.x + clampedT * dx, start.y + clampedT * dy)
    val dist = hypot(circleCenter.x - closest.x, circleCenter.y - closest.y)

    if (dist <= circleRadius) {
        return Triple(closest, dist, normal)
    }
    return null
}

/**
 * Oblicza kolizję koła ze ścianą przeszkody ([WallObstacle]).
 *
 * Działa podobnie do [getLineSegmentCollision], ale dynamicznie oblicza wektor normalny,
 * upewniając się, że zawsze wypycha on gracza na zewnątrz ściany (oblicza iloczyn skalarny
 * z wektorem od ściany do gracza). Uwzględnia również grubość ściany.
 *
 * @param circleCenter Środek koła.
 * @param circleRadius Promień koła.
 * @param wall Obiekt ściany.
 * @return [Triple] z danymi o kolizji (punkt, dystans, normalna) lub `null`.
 */
fun getWallCollisionInfo(
    circleCenter: Offset,
    circleRadius: Float,
    wall: WallObstacle
): Triple<Offset, Float, Offset>? {
    val angleRad = Math.toRadians(wall.angle.toDouble())

    // Konwersja współrzędnych biegunowych ściany na kartezjańskie końce odcinka
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

    // Uwzględnienie grubości ściany (strokeWidth = 50f, więc halfWidth = 25f)
    val wallHalfWidth = 25f
    val collisionThreshold = circleRadius + wallHalfWidth

    if (distance <= collisionThreshold) {
        val normalLen = hypot(dx, dy)
        if (normalLen == 0f) return null

        // Wstępna normalna (prostopadła do ściany)
        var normalX = -dy / normalLen
        var normalY = dx / normalLen

        // Wektor od najbliższego punktu ściany do środka gracza
        val vecToCircleX = circleCenter.x - closest.x
        val vecToCircleY = circleCenter.y - closest.y

        // Iloczyn skalarny: sprawdza, czy normalna jest skierowana w stronę gracza
        val dot = (normalX * vecToCircleX) + (normalY * vecToCircleY)

        // Jeśli normalna celuje "w drugą stronę", odwracamy ją
        if (dot < 0) {
            normalX = -normalX
            normalY = -normalY
        }

        return Triple(closest, distance, Offset(normalX, normalY))
    }

    return null
}